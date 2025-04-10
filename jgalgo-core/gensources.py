import argparse
import hashlib
import json
import logging
import shutil
import subprocess
import sys
import xml.etree.ElementTree
from pathlib import Path
from typing import Callable

TOP_DIR = Path(__file__).parent.resolve()
TEMPLATE_DIR = TOP_DIR / "template"
GENERATED_SOURCES_DIR = TOP_DIR / "src-generated"
PACKAGE_DIR = GENERATED_SOURCES_DIR / "main" / "java" / "com" / "jgalgo"
TEST_PACKAGE_DIR = GENERATED_SOURCES_DIR / "test" / "java" / "com" / "jgalgo"
TYPE_ALL = {"Obj", "Byte", "Short", "Int", "Long", "Float", "Double", "Bool", "Char"}

HASHES_FILENAME = GENERATED_SOURCES_DIR / ".gen" / "hashes.json"


def find_eclipse() -> Path | None:
    path = shutil.which("eclipse")
    if path is not None:
        return Path(path)
    path = shutil.which("Eclipse")
    if path is not None:
        return Path(path)

    match sys.platform:
        case "linux" | "linux2" | "darwin":
            path = Path("/usr/bin/eclipse")
            if path.exists():
                return path

        case "win32":
            path = Path("C:") / "Program Files" / "Eclipse" / "eclipse.exe"
            if path.exists():
                return path

    return None


def format_source_files(filenames):
    logging.info("Formatting generated files...")
    ECLIPSE_PATH = find_eclipse()
    if ECLIPSE_PATH is None:
        logging.warning("Failed to find eclipse.")
        return

    # The formatter config file is an xml file used by vscode. Eclipse uses a different format. We read the xml and write a new config file for eclipse.
    ETC_DIR = TOP_DIR.parent / "etc"
    ECLIPSE_FORMATTER_CONFIG_FILE = ETC_DIR / "eclipse-java-style.xml"

    profiles_root = xml.etree.ElementTree.parse(ECLIPSE_FORMATTER_CONFIG_FILE).getroot()
    if profiles_root.tag != "profiles":
        raise Exception(f"unexpected root tag: {profiles_root.tag}")
    profiles = [child for child in profiles_root if child.tag == "profile"]
    if len(profiles) != 1:
        raise Exception(f"unexpected number of profiles: {len(profiles)}")
    settings = [setting for setting in profiles[0] if setting.tag == "setting"]
    settings = [(setting.attrib["id"], setting.attrib["value"]) for setting in settings]

    formatter_config_file = ETC_DIR / ".eclipse-java-style"
    try:
        with open(formatter_config_file, "w") as f:
            for id, val in settings:
                f.write(f"{id}={val}\n")

        # eclipse has a command line limit
        def chunker(seq, size):
            return (seq[pos : pos + size] for pos in range(0, len(seq), size))

        for filenames_chunk in chunker(filenames, 50):
            subprocess.check_call(
                [
                    ECLIPSE_PATH,
                    "-data",
                    TOP_DIR,
                    "-nosplash",
                    "-application",
                    "org.eclipse.jdt.core.JavaCodeFormatter",
                    "-verbose",
                    "-config",
                    formatter_config_file,
                    *filenames_chunk,
                ],
                cwd=TOP_DIR,
                shell=True,
            )
    finally:
        if formatter_config_file.exists():
            formatter_config_file.unlink()


def generate_sourcefile(
    input_filename: Path, output_filename, constants: dict[str, str], functions
):
    logging.info("Generating %s", TOP_DIR / output_filename)

    def apply_function(text, func_name, func):
        begin = 0
        while True:
            begin = text.find(func_name, begin)
            if begin == -1:
                return text
            words, end = find_func_args(text, begin + len(func_name))
            text = text[:begin] + func(*words) + text[end:]

    text = open(input_filename).read()

    # Solve all '#if' and '#ifdef' directives
    root_block = {"type": "container", "blocks": []}
    stack = [root_block]
    for line in text.splitlines():
        if line.startswith("#if "):
            block = {
                "type": "if",
                "condition": line.removeprefix("#if "),
                "blocks": [],
            }
            stack[-1]["blocks"].append(block)
            stack.append(block)

        elif line.startswith("#elif "):
            stack.pop()
            block = {
                "type": "elif",
                "condition": line.removeprefix("#elif "),
                "blocks": [],
            }
            stack[-1]["blocks"].append(block)
            stack.append(block)

        elif line.startswith("#else"):
            stack.pop()
            block = {"type": "else", "blocks": []}
            stack[-1]["blocks"].append(block)
            stack.append(block)

        elif line.startswith("#endif"):
            stack.pop()
            block = {"type": "endif", "blocks": []}
            stack[-1]["blocks"].append(block)

        else:
            stack[-1]["blocks"].append(line)
    text = []

    def eval_condition(condition):
        return eval(condition, {}, constants)

    def append_lines(top_block):
        block_num = len(top_block["blocks"])
        block_idx = 0
        while block_idx < block_num:
            block = top_block["blocks"][block_idx]
            if isinstance(block, str):
                text.append(block)
                block_idx += 1
                continue
            assert block["type"] == "if", f"unexpected block type: {block['type']}"
            while True:
                if block["type"] == "else" or eval_condition(block["condition"]):
                    append_lines(block)
                    block_idx += 1
                    block = top_block["blocks"][block_idx]
                    while block["type"] != "endif":
                        block_idx += 1
                        block = top_block["blocks"][block_idx]
                    block_idx += 1
                    break

                block_idx += 1
                block = top_block["blocks"][block_idx]
                if block["type"] == "endif":
                    block_idx += 1
                    break
                assert (
                    block["type"] == "if"
                    or block["type"] == "elif"
                    or block["type"] == "else"
                )

    append_lines(root_block)
    text = "\n".join(text)

    # Add a newline at the end if there isn't one
    if len(text) == 0 or text[-1] != "\n":
        text += "\n"

    # Replace all constants one by one, in reverse sorted order to (hopefully) avoid one constant being a prefix of another
    sorted_constants = sorted(constants.items(), key=lambda kv: kv[0], reverse=True)
    for constant, value in sorted_constants:
        text = text.replace(constant, value)

    # Replace all functions calls one by one, in reverse sorted order to (hopefully) avoid one constant being a prefix of another
    sorted_functions = sorted(functions.items(), key=lambda kv: kv[0], reverse=True)
    for func_name, func in sorted_functions:
        text = apply_function(text, func_name, func)

    output_filename.parent.mkdir(parents=True, exist_ok=True)
    with open(output_filename, "w") as output_file:
        output_file.write(text)


def find_func_args(text, begin):
    if text[begin] != "(":
        raise ValueError("not a beginning of a function call")
    open_parenthesis = 1
    begin += 1
    words = []
    word = ""
    while True:
        while text[begin].isspace():
            begin += 1
        if text[begin] == "(":
            open_parenthesis += 1
            word += text[begin]
        elif text[begin] == ")":
            open_parenthesis -= 1
            if open_parenthesis == 0:
                words.append(word)
                return words, begin + 1
            word += text[begin]
        elif text[begin] == "," and open_parenthesis == 1:
            words.append(word)
            word = ""
        else:
            word += text[begin]
        begin += 1


def get_constants_and_functions_key0(key_type, generic_name):
    constants = {
        "Void": {
            "KEY_TYPE_NAME": "Void",
            "PRIMITIVE_KEY_TYPE": "Void",
            "PRIMITIVE_KEY_TYPE_REAL": "Void",
            "KEY_TYPE_GENERIC_CLASS": "Void",
            "FASTUTIL_KEY_TYPE": "_NONE_",
            "FASTUTIL_KEY_PACKAGE": "_NONE_",
        },
        "Obj": {
            "KEY_TYPE_NAME": "Obj",
            "PRIMITIVE_KEY_TYPE": generic_name,
            "PRIMITIVE_KEY_TYPE_REAL": "Object",
            "KEY_TYPE_GENERIC_CLASS": generic_name,
            "FASTUTIL_KEY_TYPE": "Object",
            "FASTUTIL_KEY_PACKAGE": "it.unimi.dsi.fastutil.objects",
        },
        "Byte": {
            "KEY_TYPE_NAME": "Byte",
            "PRIMITIVE_KEY_TYPE": "byte",
            "PRIMITIVE_KEY_TYPE_REAL": "byte",
            "KEY_TYPE_GENERIC_CLASS": "Byte",
            "FASTUTIL_KEY_TYPE": "Byte",
            "FASTUTIL_KEY_PACKAGE": "it.unimi.dsi.fastutil.bytes",
        },
        "Short": {
            "KEY_TYPE_NAME": "Short",
            "PRIMITIVE_KEY_TYPE": "short",
            "PRIMITIVE_KEY_TYPE_REAL": "short",
            "KEY_TYPE_GENERIC_CLASS": "Short",
            "FASTUTIL_KEY_TYPE": "Short",
            "FASTUTIL_KEY_PACKAGE": "it.unimi.dsi.fastutil.shorts",
        },
        "Int": {
            "KEY_TYPE_NAME": "Int",
            "PRIMITIVE_KEY_TYPE": "int",
            "PRIMITIVE_KEY_TYPE_REAL": "int",
            "KEY_TYPE_GENERIC_CLASS": "Integer",
            "FASTUTIL_KEY_TYPE": "Int",
            "FASTUTIL_KEY_PACKAGE": "it.unimi.dsi.fastutil.ints",
        },
        "Long": {
            "KEY_TYPE_NAME": "Long",
            "PRIMITIVE_KEY_TYPE": "long",
            "PRIMITIVE_KEY_TYPE_REAL": "long",
            "KEY_TYPE_GENERIC_CLASS": "Long",
            "FASTUTIL_KEY_TYPE": "Long",
            "FASTUTIL_KEY_PACKAGE": "it.unimi.dsi.fastutil.longs",
        },
        "Float": {
            "KEY_TYPE_NAME": "Float",
            "PRIMITIVE_KEY_TYPE": "float",
            "PRIMITIVE_KEY_TYPE_REAL": "float",
            "KEY_TYPE_GENERIC_CLASS": "Float",
            "FASTUTIL_KEY_TYPE": "Float",
            "FASTUTIL_KEY_PACKAGE": "it.unimi.dsi.fastutil.floats",
        },
        "Double": {
            "KEY_TYPE_NAME": "Double",
            "PRIMITIVE_KEY_TYPE": "double",
            "PRIMITIVE_KEY_TYPE_REAL": "double",
            "KEY_TYPE_GENERIC_CLASS": "Double",
            "FASTUTIL_KEY_TYPE": "Double",
            "FASTUTIL_KEY_PACKAGE": "it.unimi.dsi.fastutil.doubles",
        },
        "Bool": {
            "KEY_TYPE_NAME": "Bool",
            "PRIMITIVE_KEY_TYPE": "boolean",
            "PRIMITIVE_KEY_TYPE_REAL": "boolean",
            "KEY_TYPE_GENERIC_CLASS": "Boolean",
            "FASTUTIL_KEY_TYPE": "Boolean",
            "FASTUTIL_KEY_PACKAGE": "it.unimi.dsi.fastutil.booleans",
        },
        "Char": {
            "KEY_TYPE_NAME": "Char",
            "PRIMITIVE_KEY_TYPE": "char",
            "PRIMITIVE_KEY_TYPE_REAL": "char",
            "KEY_TYPE_GENERIC_CLASS": "Character",
            "FASTUTIL_KEY_TYPE": "Char",
            "FASTUTIL_KEY_PACKAGE": "it.unimi.dsi.fastutil.chars",
        },
    }[key_type]

    if key_type == "Obj":
        constants["KEY_TYPE_GENERIC"] = f"<{generic_name}>"
        constants["KEY_TYPE_GENERIC_IN_TEMPLATE_LIST"] = f", {generic_name}"
        constants["KEY_CAST_TO_GENERIC"] = f"({generic_name})"
        constants["KEY_SUPPRESS_WARNINGS_UNCHECKED"] = '@SuppressWarnings("unchecked")'
        constants["KEY_COMPARATOR"] = "Comparator"
    else:
        constants["KEY_TYPE_GENERIC"] = ""
        constants["KEY_TYPE_GENERIC_IN_TEMPLATE_LIST"] = ""
        constants["KEY_CAST_TO_GENERIC"] = ""
        constants["KEY_SUPPRESS_WARNINGS_UNCHECKED"] = ""
        constants["KEY_COMPARATOR"] = f"{constants['FASTUTIL_KEY_TYPE']}Comparator"

    functions = {
        "Void": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: x,
            "KEY_BOXED_TO_PRIMITIVE": lambda x: x,
        },
        "Obj": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: x,
            "KEY_BOXED_TO_PRIMITIVE": lambda x: x,
        },
        "Byte": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: f"Byte.valueOf({x})",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: f"{x}.byteValue()",
        },
        "Short": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: f"Short.valueOf({x})",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: f"{x}.shortValue()",
        },
        "Int": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: f"Integer.valueOf({x})",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: f"{x}.intValue()",
        },
        "Long": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: f"Long.valueOf({x})",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: f"{x}.longValue()",
        },
        "Float": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: f"Float.valueOf({x})",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: f"{x}.floatValue()",
        },
        "Double": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: f"Double.valueOf({x})",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: f"{x}.doubleValue()",
        },
        "Bool": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: f"Boolean.valueOf({x})",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: f"{x}.booleanValue()",
        },
        "Char": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: f"Character.valueOf({x})",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: f"{x}.charValue()",
        },
    }[key_type]

    match key_type:
        case "Obj":
            cmpDefault = lambda a, b: f"JGAlgoUtils.cmpDefault({a}, {b})"
            functions["COMPARE_KEY_DEFAULT"] = lambda a, b: cmpDefault(a, b)
            functions["COMPARE_KEY_DEFAULT_EQ"] = (
                lambda a, b: f"{cmpDefault(a, b)} == 0"
            )
            functions["COMPARE_KEY_DEFAULT_NEQ"] = (
                lambda a, b: f"{cmpDefault(a, b)} != 0"
            )
            functions["COMPARE_KEY_DEFAULT_LE"] = lambda a, b: f"{cmpDefault(a, b)} < 0"
            functions["COMPARE_KEY_DEFAULT_LEQ"] = (
                lambda a, b: f"{cmpDefault(a, b)} <= 0"
            )
            functions["COMPARE_KEY_DEFAULT_GE"] = lambda a, b: f"{cmpDefault(a, b)} > 0"
            functions["COMPARE_KEY_DEFAULT_GEQ"] = (
                lambda a, b: f"{cmpDefault(a, b)} >= 0"
            )
        case "Bool":
            functions["COMPARE_KEY_DEFAULT_EQ"] = lambda a, b: f"{a} == {b}"
            functions["COMPARE_KEY_DEFAULT_NEQ"] = lambda a, b: f"{a} != {b}"
            # functions["COMPARE_KEY_DEFAULT_LE"] = None
            # functions["COMPARE_KEY_DEFAULT_LEQ"] = None
            # functions["COMPARE_KEY_DEFAULT_GE"] = None
            # functions["COMPARE_KEY_DEFAULT_GEQ"] = None
        case _:
            cmp = constants["KEY_TYPE_GENERIC_CLASS"]
            functions["COMPARE_KEY_DEFAULT"] = lambda a, b: f"{cmp}.compare({a}, {b})"
            functions["COMPARE_KEY_DEFAULT_EQ"] = lambda a, b: f"{a} == {b}"
            functions["COMPARE_KEY_DEFAULT_NEQ"] = lambda a, b: f"{a} != {b}"
            functions["COMPARE_KEY_DEFAULT_LE"] = lambda a, b: f"{a} < {b}"
            functions["COMPARE_KEY_DEFAULT_LEQ"] = lambda a, b: f"{a} <= {b}"
            functions["COMPARE_KEY_DEFAULT_GE"] = lambda a, b: f"{a} > {b}"
            functions["COMPARE_KEY_DEFAULT_GEQ"] = lambda a, b: f"{a} >= {b}"

    return constants, functions


def get_constants_and_functions_key(key_type):
    return get_constants_and_functions_key0(key_type, "K")


def get_constants_and_functions_value(value_type):
    constants, functions = get_constants_and_functions_key0(value_type, "V")
    constants = {k.replace("KEY_", "VALUE_"): v for k, v in constants.items()}
    functions = {k.replace("KEY_", "VALUE_"): v for k, v in functions.items()}
    return constants, functions


def get_constants_and_functions_key_value(key_type, value_type):
    constants, functions = get_constants_and_functions_key(key_type)
    constants_value, functions_value = get_constants_and_functions_value(value_type)
    constants.update(constants_value)
    functions.update(functions_value)
    functions.update(functions_value)

    match (key_type, value_type):
        case ("Obj", "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<K, V>"
        case ("Obj", _):
            constants["KEY_VALUE_GENERIC"] = "<K>"
        case (_, "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<V>"
        case (_, _):
            constants["KEY_VALUE_GENERIC"] = ""
    constants["KEY_VALUE_GENERIC_EMPTY"] = (
        "" if constants["KEY_VALUE_GENERIC"] == "" else "<>"
    )
    if key_type == "Obj" and value_type == "Obj":
        constants["KEY_VALUE_PAIR"] = "Pair"
    else:
        constants["KEY_VALUE_PAIR"] = (
            f"{constants['FASTUTIL_KEY_TYPE']}{constants['FASTUTIL_VALUE_TYPE']}Pair"
        )

    return constants, functions


def get_constants_and_functions(type):
    constants, functions = get_constants_and_functions_key0(type, "T")
    constants = {k.replace("KEY_", ""): v for k, v in constants.items()}
    functions = {k.replace("KEY_", ""): v for k, v in functions.items()}
    return constants, functions


TEMPLATES = []


def register_template(template, types, config_func, file_name_func, flavor=None):
    TEMPLATES.append(
        {
            "template": f"{template}.java.template",
            "types": types,
            "config_func": config_func,
            "file_name_func": file_name_func,
            "flavor": flavor,
        }
    )


def generate_template_sources(template_entry):
    generated_files = []
    for types in template_entry["types"]:
        match template_entry["flavor"]:
            case None if type(types) is tuple:
                assert len(types) == 2
                constants, functions = get_constants_and_functions_key_value(*types)
            case None:
                constants, functions = get_constants_and_functions(types)
            case "key":
                constants, functions = get_constants_and_functions_key(types)
            case "key_value":
                constants, functions = get_constants_and_functions_key_value(*types)
            case "value":
                constants, functions = get_constants_and_functions_value(types)
            case "element":
                constants, functions = get_constants_and_functions(types)
            case unknown:
                raise Exception(f"Unknown flavor: {unknown}")

        if type(types) is tuple:
            assert len(types) == 2
            types = list(types)
        else:
            types = [types]
        template_entry["config_func"](*types, constants, functions)

        filename = template_entry["file_name_func"](*types)
        try:
            generate_sourcefile(
                TEMPLATE_DIR / template_entry["template"],
                filename,
                constants,
                functions,
            )
        except Exception as e:
            raise Exception(f"Failed to generate {filename}") from e
        generated_files.append(filename)
    return generated_files


def key_value_prefix(key_type: str, value_type: str) -> str:
    return key_type + value_type if value_type != "Void" else key_type


def generate_weights(type, constants, functions):
    constants["IWEIGHTS"] = f"IWeights{type}"
    constants["WEIGHTS"] = f"Weights{type}"


register_template(
    "Weights",
    TYPE_ALL,
    generate_weights,
    lambda type: PACKAGE_DIR / "graph" / f"Weights{type}.java",
)


def generate_iweights(type, constants, functions):
    constants["IWEIGHTS"] = f"IWeights{type}"
    constants["WEIGHTS"] = f"Weights{type}"


register_template(
    "IWeights",
    TYPE_ALL,
    generate_iweights,
    lambda type: PACKAGE_DIR / "graph" / f"IWeights{type}.java",
)


def generate_weights_impl(type, constants, functions):
    constants["WEIGHTS_IMPL"] = f"WeightsImpl{type}"
    constants["IWEIGHTS"] = f"IWeights{type}"
    constants["WEIGHTS"] = f"Weights{type}"


register_template(
    "WeightsImpl",
    TYPE_ALL,
    generate_weights_impl,
    lambda type: PACKAGE_DIR / "graph" / f"WeightsImpl{type}.java",
)


def generate_referenceable_heap(key_type: str, value_type: str, constants, functions):
    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["HEAP_REFERENCE"] = f"{prefix}ReferenceableHeap.Ref"
    constants["PAIRING_HEAP"] = f"{prefix}PairingHeap"


register_template(
    "ReferenceableHeap",
    [
        ("Int", "Int"),
        ("Int", "Void"),
        ("Long", "Int"),
        ("Double", "Int"),
        ("Double", "Obj"),
        ("Obj", "Void"),
        ("Obj", "Obj"),
    ],
    generate_referenceable_heap,
    lambda key_type, value_type: PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "ReferenceableHeap.java"),
)


def generate_referenceable_heap_test_utils(
    key_type: str, value_type: str, constants, functions
):
    if key_type == "Obj":
        constants["PRIMITIVE_KEY_TYPE"] = "String"
        constants["KEY_TYPE_GENERIC"] = "<String>"
        constants["KEY_TYPE_GENERIC_CLASS"] = "String"

    if value_type == "Obj":
        constants["PRIMITIVE_VALUE_TYPE"] = "String"

    match (key_type, value_type):
        case ("Obj", "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String, String>"
        case ("Obj", _):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, _):
            constants["KEY_VALUE_GENERIC"] = ""

    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP_TEST_UTILS"] = f"{prefix}ReferenceableHeapTestUtils"
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["HEAP_REFERENCE"] = f"{prefix}ReferenceableHeap.Ref"


register_template(
    "ReferenceableHeapTestUtils",
    [
        ("Int", "Int"),
        ("Int", "Void"),
        ("Long", "Int"),
        ("Double", "Int"),
        ("Double", "Obj"),
        ("Obj", "Void"),
        ("Obj", "Obj"),
    ],
    generate_referenceable_heap_test_utils,
    lambda key_type, value_type: TEST_PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "ReferenceableHeapTestUtils.java"),
)


def generate_pairing_heap(key_type: str, value_type: str, constants, functions):
    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["HEAP_REFERENCE"] = f"{prefix}ReferenceableHeap.Ref"
    constants["PAIRING_HEAP"] = f"{prefix}PairingHeap"


register_template(
    "PairingHeap",
    [
        ("Int", "Int"),
        ("Int", "Void"),
        ("Long", "Int"),
        ("Double", "Int"),
        ("Double", "Obj"),
        ("Obj", "Void"),
        ("Obj", "Obj"),
    ],
    generate_pairing_heap,
    lambda key_type, value_type: PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "PairingHeap.java"),
)


def generate_pairing_heap_test(key_type: str, value_type: str, constants, functions):
    prefix = key_value_prefix(key_type, value_type)
    constants["PAIRING_HEAP"] = f"{prefix}PairingHeap"
    constants["PAIRING_HEAP_TEST"] = f"{prefix}PairingHeapTest"
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["HEAP_REFERENCE"] = f"{prefix}ReferenceableHeap.Ref"
    constants["REFERENCEABLE_HEAP_TEST_UTILS"] = f"{prefix}ReferenceableHeapTestUtils"
    if key_type == "Obj":
        constants["PRIMITIVE_KEY_TYPE"] = "String"
        constants["KEY_TYPE_GENERIC"] = "<String>"
    match (key_type, value_type):
        case ("Obj", "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String, String>"
        case ("Obj", _):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, _):
            constants["KEY_VALUE_GENERIC"] = ""


register_template(
    "PairingHeapTest",
    [
        ("Int", "Int"),
        ("Int", "Void"),
        ("Long", "Int"),
        ("Double", "Int"),
        ("Double", "Obj"),
        ("Obj", "Void"),
        ("Obj", "Obj"),
    ],
    generate_pairing_heap_test,
    lambda key_type, value_type: TEST_PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "PairingHeapTest.java"),
)


def generate_binomial_heap(key_type: str, value_type: str, constants, functions):
    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["HEAP_REFERENCE"] = f"{prefix}ReferenceableHeap.Ref"
    constants["BINOMIAL_HEAP"] = f"{prefix}BinomialHeap"


register_template(
    "BinomialHeap",
    [("Int", "Int"), ("Obj", "Obj")],
    generate_binomial_heap,
    lambda key_type, value_type: PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "BinomialHeap.java"),
)


def generate_binomial_heap_test(key_type: str, value_type: str, constants, functions):
    prefix = key_value_prefix(key_type, value_type)
    constants["BINOMIAL_HEAP"] = f"{prefix}BinomialHeap"
    constants["BINOMIAL_HEAP_TEST"] = f"{prefix}BinomialHeapTest"
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["REFERENCEABLE_HEAP_TEST_UTILS"] = f"{prefix}ReferenceableHeapTestUtils"
    constants["PAIRING_HEAP"] = f"{prefix}PairingHeap"
    if key_type == "Obj":
        constants["KEY_TYPE_GENERIC"] = "<String>"
    match (key_type, value_type):
        case ("Obj", "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String, String>"
        case ("Obj", _):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, _):
            constants["KEY_VALUE_GENERIC"] = ""


register_template(
    "BinomialHeapTest",
    [("Int", "Int"), ("Obj", "Obj")],
    generate_binomial_heap_test,
    lambda key_type, value_type: TEST_PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "BinomialHeapTest.java"),
)


def generate_fibonacci_heap(key_type: str, value_type: str, constants, functions):
    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["HEAP_REFERENCE"] = f"{prefix}ReferenceableHeap.Ref"
    constants["FIBONACCI_HEAP"] = f"{prefix}FibonacciHeap"


register_template(
    "FibonacciHeap",
    [("Int", "Int"), ("Obj", "Obj")],
    generate_fibonacci_heap,
    lambda key_type, value_type: PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "FibonacciHeap.java"),
)


def generate_fibonacci_heap_test(key_type: str, value_type: str, constants, functions):
    prefix = key_value_prefix(key_type, value_type)
    constants["FIBONACCI_HEAP"] = f"{prefix}FibonacciHeap"
    constants["FIBONACCI_HEAP_TEST"] = f"{prefix}FibonacciHeapTest"
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["REFERENCEABLE_HEAP_TEST_UTILS"] = f"{prefix}ReferenceableHeapTestUtils"
    constants["PAIRING_HEAP"] = f"{prefix}PairingHeap"
    if key_type == "Obj":
        constants["KEY_TYPE_GENERIC"] = "<String>"
    match (key_type, value_type):
        case ("Obj", "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String, String>"
        case ("Obj", _):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, _):
            constants["KEY_VALUE_GENERIC"] = ""


register_template(
    "FibonacciHeapTest",
    [("Int", "Int"), ("Obj", "Obj")],
    generate_fibonacci_heap_test,
    lambda key_type, value_type: TEST_PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "FibonacciHeapTest.java"),
)


def generate_binary_search_tree(
    key_type: str, value_type: str, constants: dict[str, str], functions
):
    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["HEAP_REFERENCE"] = f"{prefix}ReferenceableHeap.Ref"
    constants["BINARY_SEARCH_TREE"] = f"{prefix}BinarySearchTree"
    constants["RED_BLACK_TREE"] = f"{prefix}RedBlackTree"


register_template(
    "BinarySearchTree",
    [("Int", "Int"), ("Obj", "Obj"), ("Double", "Obj")],
    generate_binary_search_tree,
    lambda key_type, value_type: PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "BinarySearchTree.java"),
)


def generate_binary_search_tree_test_utils(
    key_type: str, value_type: str, constants: dict[str, str], functions
):
    if key_type == "Obj":
        constants["PRIMITIVE_KEY_TYPE"] = "String"
        constants["KEY_TYPE_GENERIC"] = "<String>"
        constants["KEY_TYPE_GENERIC_CLASS"] = "String"

    if value_type == "Obj":
        constants["PRIMITIVE_VALUE_TYPE"] = "String"

    match (key_type, value_type):
        case ("Obj", "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String, String>"
        case ("Obj", _):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, _):
            constants["KEY_VALUE_GENERIC"] = ""

    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["REFERENCEABLE_HEAP_TEST_UTILS"] = f"{prefix}ReferenceableHeapTestUtils"
    constants["HEAP_REFERENCE"] = f"{prefix}ReferenceableHeap.Ref"
    constants["BINARY_SEARCH_TREE"] = f"{prefix}BinarySearchTree"
    constants["BINARY_SEARCH_TREE_TEST_UTILS"] = f"{prefix}BinarySearchTreeTestUtils"


register_template(
    "BinarySearchTreeTestUtils",
    [("Int", "Int"), ("Obj", "Obj"), ("Double", "Obj")],
    generate_binary_search_tree_test_utils,
    lambda key_type, value_type: TEST_PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "BinarySearchTreeTestUtils.java"),
)


def generate_binary_search_trees(key_type: str, constants: dict[str, str], functions):
    constants["BINARY_SEARCH_TREES"] = f"{key_type}BinarySearchTrees"
    if key_type == "Obj":
        constants["KEY_GENERIC_LIST_START"] = "K, "
    else:
        constants["KEY_GENERIC_LIST_START"] = ""


register_template(
    "BinarySearchTrees",
    ["Int", "Double", "Obj"],
    generate_binary_search_trees,
    lambda key_type: PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_type + "BinarySearchTrees.java"),
    flavor="key",
)


def generate_red_black_tree(
    key_type: str, value_type: str, constants: dict[str, str], functions
):
    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["HEAP_REFERENCE"] = f"{prefix}ReferenceableHeap.Ref"
    constants["BINARY_SEARCH_TREE"] = f"{prefix}BinarySearchTree"
    constants["BINARY_SEARCH_TREES"] = f"{key_type}BinarySearchTrees"
    constants["RED_BLACK_TREE"] = f"{prefix}RedBlackTree"
    if key_type == "Obj":
        constants["KEY_GENERIC_LIST_START"] = "K, "
    else:
        constants["KEY_GENERIC_LIST_START"] = ""


register_template(
    "RedBlackTree",
    [("Int", "Int"), ("Obj", "Obj"), ("Double", "Obj")],
    generate_red_black_tree,
    lambda key_type, value_type: PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "RedBlackTree.java"),
)


def generate_red_black_tree_test(
    key_type: str, value_type: str, constants: dict[str, str], functions
):
    prefix = key_value_prefix(key_type, value_type)
    constants["RED_BLACK_TREE"] = f"{prefix}RedBlackTree"
    constants["RED_BLACK_TREE_TEST"] = f"{prefix}RedBlackTreeTest"
    constants["BINARY_SEARCH_TREE"] = f"{prefix}BinarySearchTree"
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["REFERENCEABLE_HEAP_TEST_UTILS"] = f"{prefix}ReferenceableHeapTestUtils"
    constants["BINARY_SEARCH_TREE_TEST_UTILS"] = f"{prefix}BinarySearchTreeTestUtils"
    constants["PAIRING_HEAP"] = f"{prefix}PairingHeap"
    if key_type == "Obj":
        constants["KEY_TYPE_GENERIC"] = "<String>"
    match (key_type, value_type):
        case ("Obj", "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String, String>"
        case ("Obj", _):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, _):
            constants["KEY_VALUE_GENERIC"] = ""


register_template(
    "RedBlackTreeTest",
    [("Int", "Int"), ("Obj", "Obj"), ("Double", "Obj")],
    generate_red_black_tree_test,
    lambda key_type, value_type: TEST_PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "RedBlackTreeTest.java"),
)


def generate_splay_tree(
    key_type: str, value_type: str, constants: dict[str, str], functions
):
    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["HEAP_REFERENCE"] = f"{prefix}ReferenceableHeap.Ref"
    constants["BINARY_SEARCH_TREE"] = f"{prefix}BinarySearchTree"
    constants["BINARY_SEARCH_TREES"] = f"{key_type}BinarySearchTrees"
    constants["SPLAY_TREE"] = f"{prefix}SplayTree"
    if key_type == "Obj":
        constants["KEY_GENERIC_LIST_START"] = "K, "
    else:
        constants["KEY_GENERIC_LIST_START"] = ""


register_template(
    "SplayTree",
    [("Int", "Int"), ("Obj", "Obj")],
    generate_splay_tree,
    lambda key_type, value_type: PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "SplayTree.java"),
)


def generate_splay_tree_test(
    key_type: str, value_type: str, constants: dict[str, str], functions
):
    prefix = key_value_prefix(key_type, value_type)
    constants["SPLAY_TREE"] = f"{prefix}SplayTree"
    constants["SPLAY_TREE_TEST"] = f"{prefix}SplayTreeTest"
    constants["REFERENCEABLE_HEAP"] = f"{prefix}ReferenceableHeap"
    constants["REFERENCEABLE_HEAP_TEST_UTILS"] = f"{prefix}ReferenceableHeapTestUtils"
    constants["BINARY_SEARCH_TREE_TEST_UTILS"] = f"{prefix}BinarySearchTreeTestUtils"
    constants["PAIRING_HEAP"] = f"{prefix}PairingHeap"
    if key_type == "Obj":
        constants["KEY_TYPE_GENERIC"] = "<String>"
    match (key_type, value_type):
        case ("Obj", "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String, String>"
        case ("Obj", _):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, "Obj"):
            constants["KEY_VALUE_GENERIC"] = "<String>"
        case (_, _):
            constants["KEY_VALUE_GENERIC"] = ""


register_template(
    "SplayTreeTest",
    [("Int", "Int"), ("Obj", "Obj")],
    generate_splay_tree_test,
    lambda key_type, value_type: TEST_PACKAGE_DIR
    / "internal"
    / "ds"
    / (key_value_prefix(key_type, value_type) + "SplayTreeTest.java"),
)


def clean():
    logging.info("Cleaning generated sources...")
    if HASHES_FILENAME.exists():
        HASHES_FILENAME.unlink()
    shutil.rmtree(GENERATED_SOURCES_DIR, ignore_errors=True)


def compute_template_hash(template_filename: Path):
    with open(template_filename, "rb") as template_file:
        template_content = template_file.read()
    return hashlib.md5(template_content).hexdigest()


def read_last_generated_templates_hashes() -> Callable[[str], bool]:
    hashes = {}
    if HASHES_FILENAME.exists():
        with open(HASHES_FILENAME) as hashes_file:
            hashes = {
                Path(tmp): hash for tmp, hash in json.loads(hashes_file.read()).items()
            }

    def is_template_changed(template_filename: str):
        template_file = TEMPLATE_DIR / template_filename
        template_hash = compute_template_hash(template_file)
        return hashes.get(template_file) != template_hash

    return is_template_changed


def write_generated_templates():
    templates = [TEMPLATE_DIR / generator["template"] for generator in TEMPLATES]
    hashes = json.dumps(
        {str(temp.resolve()): compute_template_hash(temp) for temp in templates}
    )

    HASHES_FILENAME.parent.mkdir(parents=True, exist_ok=True)
    with open(HASHES_FILENAME, "w") as hashes_file:
        hashes_file.write(hashes)


def main():
    logging.basicConfig(format="[%(levelname)s] %(message)s", level=logging.INFO)

    parser = argparse.ArgumentParser(description="Auto source generator")
    parser.add_argument("--clean", action="store_true")
    args = parser.parse_args()

    if args.clean:
        clean()

    else:
        is_template_changed_func = read_last_generated_templates_hashes()
        generated_files = []

        is_template_changed = False
        for generator in TEMPLATES:
            if is_template_changed_func(generator["template"]):
                is_template_changed = True
                generated_files += generate_template_sources(generator)

        if not is_template_changed:
            logging.info("No template changed, nothing to do.")
            return

        format_source_files(generated_files)

        write_generated_templates()


if __name__ == "__main__":
    main()
