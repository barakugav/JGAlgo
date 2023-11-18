import os
import sys
import shutil
import subprocess
import functools
import json
import argparse
import logging


TOP_DIR = os.path.dirname(os.path.realpath(__file__))
TEMPLATE_DIR = os.path.join(TOP_DIR, "template")
SOURCE_DIR = os.path.join(TOP_DIR, "src", "main", "java")
PACKAGE_DIR = os.path.join(SOURCE_DIR, "com", "jgalgo")
TYPE_ALL = {"Obj", "Byte", "Short", "Int", "Long", "Float", "Double", "Bool", "Char"}

HASHES_FILENAME = os.path.join(TOP_DIR, ".gen", "hashes.json")


def find_eclipse():
    path = shutil.which("eclipse")
    if path is not None:
        return path
    path = shutil.which("Eclipse")
    if path is not None:
        return path

    if sys.platform == "linux" or sys.platform == "linux2":
        path = os.path.join(os.sep, "usr", "bin", "eclipse")
        if os.path.exists(path):
            return path

    elif sys.platform == "win32":
        path = os.path.join("C:", "Program Files", "Eclipse", "eclipse.exe")
        if os.path.exists(path):
            return path

    return None


def format_sourcefiles(filenames):
    logging.info("Formatting generated files...")
    ECLIPSE_PATH = find_eclipse()
    if ECLIPSE_PATH is None:
        logging.warning("Failed to find eclipse.")
        return
    ECLIPSE_FORMATTER_CONFIG_FILE = os.path.abspath(
        os.path.join(TOP_DIR, "..", "ect", "eclipse-java-style.xml")
    )
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
            os.path.join(ECLIPSE_FORMATTER_CONFIG_FILE, "org.eclipse.jdt.core.prefs"),
            *filenames,
        ],
        cwd=TOP_DIR,
        shell=True,
    )


def generate_sourcefile(input_filename, output_filename, constants, functions):
    logging.info("Generating %s from %s", output_filename, input_filename)

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
                "condition": line[len("#if ") :],
                "blocks": [],
            }
            stack[-1]["blocks"].append(block)
            stack.append(block)

        elif line.startswith("#elif "):
            stack.pop()
            block = {"type": "elif", "condition": line[len("#elif ") :], "blocks": []}
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
            assert block["type"] == "if", "unexpected block type: " + block["type"]
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

    # Replace all constants one by one, in reverse sorted order to (hopefully) avoid one constant being a prefix of another
    sorted_constants = sorted(constants.items(), key=lambda kv: kv[0], reverse=True)
    for constant, value in sorted_constants:
        text = text.replace(constant, value)

    # Replace all functions calls one by one, in reverse sorted order to (hopefully) avoid one constant being a prefix of another
    sorted_functions = sorted(functions.items(), key=lambda kv: kv[0], reverse=True)
    for func_name, func in sorted_functions:
        text = apply_function(text, func_name, func)

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
        constants["KEY_TYPE_GENERIC"] = "<" + generic_name + ">"
        constants["KEY_TYPE_GENERIC_IN_TEMPLATE_LIST"] = ", " + generic_name
        constants["KEY_CAST_TO_GENERIC"] = "(" + generic_name + ")"
        constants["KEY_SUPPRESS_WARNINGS_UNCHECKED"] = '@SuppressWarnings("unchecked")'
        constants["KEY_COMPARATOR"] = "Comparator"
    else:
        constants["KEY_TYPE_GENERIC"] = ""
        constants["KEY_TYPE_GENERIC_IN_TEMPLATE_LIST"] = ""
        constants["KEY_CAST_TO_GENERIC"] = ""
        constants["KEY_SUPPRESS_WARNINGS_UNCHECKED"] = ""
        constants["KEY_COMPARATOR"] = constants["FASTUTIL_KEY_TYPE"] + "Comparator"

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
            "KEY_PRIMITIVE_TO_BOXED": lambda x: "Byte.valueOf(" + x + ")",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: x + ".byteValue()",
        },
        "Short": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: "Short.valueOf(" + x + ")",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: x + ".shortValue()",
        },
        "Int": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: "Integer.valueOf(" + x + ")",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: x + ".intValue()",
        },
        "Long": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: "Long.valueOf(" + x + ")",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: x + ".longValue()",
        },
        "Float": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: "Float.valueOf(" + x + ")",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: x + ".floatValue()",
        },
        "Double": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: "Double.valueOf(" + x + ")",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: x + ".doubleValue()",
        },
        "Bool": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: "Boolean.valueOf(" + x + ")",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: x + ".booleanValue()",
        },
        "Char": {
            "KEY_PRIMITIVE_TO_BOXED": lambda x: "Character.valueOf(" + x + ")",
            "KEY_BOXED_TO_PRIMITIVE": lambda x: x + ".charValue()",
        },
    }[key_type]

    if key_type == "Obj":
        cmpDefault = lambda a, b: "JGAlgoUtils.cmpDefault(" + a + ", " + b + ")"
        functions["COMPARE_KEY_DEFAULT"] = lambda a, b: cmpDefault(a, b)
        functions["COMPARE_KEY_DEFAULT_EQ"] = lambda a, b: cmpDefault(a, b) + " == 0"
        functions["COMPARE_KEY_DEFAULT_NEQ"] = lambda a, b: cmpDefault(a, b) + " != 0"
        functions["COMPARE_KEY_DEFAULT_LE"] = lambda a, b: cmpDefault(a, b) + " < 0"
        functions["COMPARE_KEY_DEFAULT_LEQ"] = lambda a, b: cmpDefault(a, b) + " <= 0"
        functions["COMPARE_KEY_DEFAULT_GE"] = lambda a, b: cmpDefault(a, b) + " > 0"
        functions["COMPARE_KEY_DEFAULT_GEQ"] = lambda a, b: cmpDefault(a, b) + " >= 0"
    elif key_type == "Bool":
        functions["COMPARE_KEY_DEFAULT_EQ"] = lambda a, b: a + " == " + b
        functions["COMPARE_KEY_DEFAULT_NEQ"] = lambda a, b: a + " != " + b
        # functions["COMPARE_KEY_DEFAULT_LE"] = None
        # functions["COMPARE_KEY_DEFAULT_LEQ"] = None
        # functions["COMPARE_KEY_DEFAULT_GE"] = None
        # functions["COMPARE_KEY_DEFAULT_GEQ"] = None
    else:
        cmp = constants["KEY_TYPE_GENERIC_CLASS"]
        functions["COMPARE_KEY_DEFAULT"] = (
            lambda a, b: cmp + ".compare(" + a + ", " + b + ")"
        )
        functions["COMPARE_KEY_DEFAULT_EQ"] = lambda a, b: a + " == " + b
        functions["COMPARE_KEY_DEFAULT_NEQ"] = lambda a, b: a + " != " + b
        functions["COMPARE_KEY_DEFAULT_LE"] = lambda a, b: a + " < " + b
        functions["COMPARE_KEY_DEFAULT_LEQ"] = lambda a, b: a + " <= " + b
        functions["COMPARE_KEY_DEFAULT_GE"] = lambda a, b: a + " > " + b
        functions["COMPARE_KEY_DEFAULT_GEQ"] = lambda a, b: a + " >= " + b

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

    if key_type == "Obj" and value_type == "Obj":
        constants["KEY_VALUE_GENERIC"] = "<K, V>"
    elif key_type == "Obj":
        constants["KEY_VALUE_GENERIC"] = "<K>"
    elif value_type == "Obj":
        constants["KEY_VALUE_GENERIC"] = "<V>"
    else:
        constants["KEY_VALUE_GENERIC"] = ""
    constants["KEY_VALUE_GENERIC_EMPTY"] = (
        "" if constants["KEY_VALUE_GENERIC"] == "" else "<>"
    )

    return constants, functions


def get_constants_and_functions(type):
    constants, functions = get_constants_and_functions_key0(type, "T")
    constants = {k.replace("KEY_", ""): v for k, v in constants.items()}
    functions = {k.replace("KEY_", ""): v for k, v in functions.items()}
    return constants, functions


def weights_filename(type):
    return os.path.join(PACKAGE_DIR, "graph", "Weights" + type + ".java")


def iweights_filename(type):
    return os.path.join(PACKAGE_DIR, "graph", "IWeights" + type + ".java")


def weights_impl_filename(type):
    return os.path.join(PACKAGE_DIR, "graph", "WeightsImpl" + type + ".java")


def key_value_prefix(key_type, value_type):
    return key_type + value_type if value_type != "Void" else key_type


def referenceable_heap_filename(key_type, value_type):
    prefix = key_value_prefix(key_type, value_type)
    return os.path.join(
        PACKAGE_DIR, "internal", "ds", prefix + "ReferenceableHeap.java"
    )


def pairing_heap_filename(key_type, value_type):
    prefix = key_value_prefix(key_type, value_type)
    return os.path.join(PACKAGE_DIR, "internal", "ds", prefix + "PairingHeap.java")


def binomial_heap_filename(key_type, value_type):
    prefix = key_value_prefix(key_type, value_type)
    return os.path.join(PACKAGE_DIR, "internal", "ds", prefix + "BinomialHeap.java")


def fibonacci_heap_filename(key_type, value_type):
    prefix = key_value_prefix(key_type, value_type)
    return os.path.join(PACKAGE_DIR, "internal", "ds", prefix + "FibonacciHeap.java")


def binary_search_tree_filename(key_type, value_type):
    prefix = key_value_prefix(key_type, value_type)
    return os.path.join(PACKAGE_DIR, "internal", "ds", prefix + "BinarySearchTree.java")


def binary_search_trees_filename(key_type):
    return os.path.join(
        PACKAGE_DIR, "internal", "ds", key_type + "BinarySearchTrees.java"
    )


def red_black_tree_filename(key_type, value_type):
    prefix = key_value_prefix(key_type, value_type)
    return os.path.join(PACKAGE_DIR, "internal", "ds", prefix + "RedBlackTree.java")


def splay_tree_filename(key_type, value_type):
    prefix = key_value_prefix(key_type, value_type)
    return os.path.join(PACKAGE_DIR, "internal", "ds", prefix + "SplayTree.java")


def generate_weights(type):
    constants, functions = get_constants_and_functions(type)
    constants["IWEIGHTS"] = "IWeights" + type
    constants["WEIGHTS"] = "Weights" + type

    generate_sourcefile(
        os.path.join(TEMPLATE_DIR, "Weights.java.template"),
        weights_filename(type),
        constants,
        functions,
    )


def generate_iweights(type):
    constants, functions = get_constants_and_functions(type)
    constants["IWEIGHTS"] = "IWeights" + type
    constants["WEIGHTS"] = "Weights" + type

    generate_sourcefile(
        os.path.join(TEMPLATE_DIR, "IWeights.java.template"),
        iweights_filename(type),
        constants,
        functions,
    )


def generate_weights_impl(type):
    constants, functions = get_constants_and_functions(type)
    constants["WEIGHTS_IMPL"] = "WeightsImpl" + type
    constants["IWEIGHTS"] = "IWeights" + type
    constants["WEIGHTS"] = "Weights" + type

    generate_sourcefile(
        os.path.join(TEMPLATE_DIR, "WeightsImpl.java.template"),
        weights_impl_filename(type),
        constants,
        functions,
    )


def generate_referenceable_heap(key_type, value_type):
    constants, functions = get_constants_and_functions_key_value(key_type, value_type)

    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = prefix + "ReferenceableHeap"
    constants["HEAP_REFERENCE"] = prefix + "ReferenceableHeap.Ref"
    constants["PAIRING_HEAP"] = prefix + "PairingHeap"

    generate_sourcefile(
        os.path.join(TEMPLATE_DIR, "ReferenceableHeap.java.template"),
        referenceable_heap_filename(key_type, value_type),
        constants,
        functions,
    )


def generate_pairing_heap(key_type, value_type):
    constants, functions = get_constants_and_functions_key_value(key_type, value_type)

    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = prefix + "ReferenceableHeap"
    constants["HEAP_REFERENCE"] = prefix + "ReferenceableHeap.Ref"
    constants["PAIRING_HEAP"] = prefix + "PairingHeap"

    generate_sourcefile(
        os.path.join(TEMPLATE_DIR, "PairingHeap.java.template"),
        pairing_heap_filename(key_type, value_type),
        constants,
        functions,
    )


def generate_binomial_heap(key_type, value_type):
    constants, functions = get_constants_and_functions_key_value(key_type, value_type)

    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = prefix + "ReferenceableHeap"
    constants["HEAP_REFERENCE"] = prefix + "ReferenceableHeap.Ref"
    constants["BINOMIAL_HEAP"] = prefix + "BinomialHeap"

    generate_sourcefile(
        os.path.join(TEMPLATE_DIR, "BinomialHeap.java.template"),
        binomial_heap_filename(key_type, value_type),
        constants,
        functions,
    )


def generate_fibonacci_heap(key_type, value_type):
    constants, functions = get_constants_and_functions_key_value(key_type, value_type)

    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = prefix + "ReferenceableHeap"
    constants["HEAP_REFERENCE"] = prefix + "ReferenceableHeap.Ref"
    constants["FIBONACCI_HEAP"] = prefix + "FibonacciHeap"

    generate_sourcefile(
        os.path.join(TEMPLATE_DIR, "FibonacciHeap.java.template"),
        fibonacci_heap_filename(key_type, value_type),
        constants,
        functions,
    )


def generate_binary_search_tree(key_type, value_type):
    constants, functions = get_constants_and_functions_key_value(key_type, value_type)

    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = prefix + "ReferenceableHeap"
    constants["HEAP_REFERENCE"] = prefix + "ReferenceableHeap.Ref"
    constants["BINARY_SEARCH_TREE"] = prefix + "BinarySearchTree"
    constants["RED_BLACK_TREE"] = prefix + "RedBlackTree"

    generate_sourcefile(
        os.path.join(TEMPLATE_DIR, "BinarySearchTree.java.template"),
        binary_search_tree_filename(key_type, value_type),
        constants,
        functions,
    )


def generate_binary_search_trees(key_type):
    constants, functions = get_constants_and_functions_key(key_type)

    constants["BINARY_SEARCH_TREES"] = key_type + "BinarySearchTrees"
    if key_type == "Obj":
        constants["KEY_GENERIC_LIST_START"] = "K, "
    else:
        constants["KEY_GENERIC_LIST_START"] = ""

    generate_sourcefile(
        os.path.join(TEMPLATE_DIR, "BinarySearchTrees.java.template"),
        binary_search_trees_filename(key_type),
        constants,
        functions,
    )


def generate_red_black_tree(key_type, value_type):
    constants, functions = get_constants_and_functions_key_value(key_type, value_type)

    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = prefix + "ReferenceableHeap"
    constants["HEAP_REFERENCE"] = prefix + "ReferenceableHeap.Ref"
    constants["BINARY_SEARCH_TREE"] = prefix + "BinarySearchTree"
    constants["BINARY_SEARCH_TREES"] = key_type + "BinarySearchTrees"
    constants["RED_BLACK_TREE"] = prefix + "RedBlackTree"
    if key_type == "Obj":
        constants["KEY_GENERIC_LIST_START"] = "K, "
    else:
        constants["KEY_GENERIC_LIST_START"] = ""

    generate_sourcefile(
        os.path.join(TEMPLATE_DIR, "RedBlackTree.java.template"),
        red_black_tree_filename(key_type, value_type),
        constants,
        functions,
    )


def generate_splay_tree(key_type, value_type):
    constants, functions = get_constants_and_functions_key_value(key_type, value_type)

    prefix = key_value_prefix(key_type, value_type)
    constants["REFERENCEABLE_HEAP"] = prefix + "ReferenceableHeap"
    constants["HEAP_REFERENCE"] = prefix + "ReferenceableHeap.Ref"
    constants["BINARY_SEARCH_TREE"] = prefix + "BinarySearchTree"
    constants["BINARY_SEARCH_TREES"] = key_type + "BinarySearchTrees"
    constants["SPLAY_TREE"] = prefix + "SplayTree"
    if key_type == "Obj":
        constants["KEY_GENERIC_LIST_START"] = "K, "
    else:
        constants["KEY_GENERIC_LIST_START"] = ""

    generate_sourcefile(
        os.path.join(TEMPLATE_DIR, "SplayTree.java.template"),
        splay_tree_filename(key_type, value_type),
        constants,
        functions,
    )


def clean():
    logging.info("Cleaning generated sources...")
    if os.path.exists(HASHES_FILENAME):
        os.remove(HASHES_FILENAME)

    generated_filenames = []
    for type in TYPE_ALL:
        generated_filenames.append(weights_filename(type))
    for type in TYPE_ALL:
        generated_filenames.append(iweights_filename(type))
    for type in TYPE_ALL:
        generated_filenames.append(weights_impl_filename(type))
    for key_type in TYPE_ALL - {"Bool"}:
        for value_type in TYPE_ALL | {"Void"}:
            generated_filenames.append(
                referenceable_heap_filename(key_type, value_type)
            )
    for key_type in TYPE_ALL - {"Bool"}:
        for value_type in TYPE_ALL | {"Void"}:
            generated_filenames.append(pairing_heap_filename(key_type, value_type))
    for key_type in TYPE_ALL - {"Bool"}:
        for value_type in TYPE_ALL | {"Void"}:
            generated_filenames.append(binomial_heap_filename(key_type, value_type))
    for key_type in TYPE_ALL - {"Bool"}:
        for value_type in TYPE_ALL | {"Void"}:
            generated_filenames.append(fibonacci_heap_filename(key_type, value_type))
    for key_type in TYPE_ALL - {"Bool"}:
        for value_type in TYPE_ALL | {"Void"}:
            generated_filenames.append(
                binary_search_tree_filename(key_type, value_type)
            )
    for key_type in TYPE_ALL - {"Bool"}:
        for value_type in TYPE_ALL | {"Void"}:
            generated_filenames.append(binary_search_trees_filename(key_type))
    for key_type in TYPE_ALL - {"Bool"}:
        for value_type in TYPE_ALL | {"Void"}:
            generated_filenames.append(red_black_tree_filename(key_type, value_type))

    for key_type in TYPE_ALL - {"Bool"}:
        for value_type in TYPE_ALL | {"Void"}:
            generated_filenames.append(splay_tree_filename(key_type, value_type))

    for filename in generated_filenames:
        if os.path.exists(filename):
            logging.debug("Removing %s", filename)
            os.remove(filename)


def compute_template_hash(template_filename):
    import hashlib

    with open(template_filename, "rb") as template_file:
        template_content = template_file.read()
    h = hashlib.md5(template_content)
    return hashlib.md5(template_content).hexdigest()


def read_last_generated_templates_hashes():
    hashes = {}
    if os.path.exists(HASHES_FILENAME):
        with open(HASHES_FILENAME) as hashes_file:
            hashes = json.load(hashes_file)

    def is_template_changed(template_filename):
        template_filename = os.path.join(TEMPLATE_DIR, template_filename)
        template_hash = compute_template_hash(template_filename)
        return (
            template_filename not in hashes
            or hashes[template_filename] != template_hash
        )

    class Object:
        pass

    ret = Object()
    ret.is_template_changed = is_template_changed
    return ret


def write_generated_templates():
    templates = [
        os.path.join(TEMPLATE_DIR, temp)
        for temp in (
            "Weights.java.template",
            "IWeights.java.template",
            "WeightsImpl.java.template",
            "ReferenceableHeap.java.template",
            "PairingHeap.java.template",
            "BinarySearchTree.java.template",
            "BinarySearchTrees.java.template",
            "RedBlackTree.java.template",
            "SplayTree.java.template",
        )
    ]
    hashes = json.dumps({temp: compute_template_hash(temp) for temp in templates})

    os.makedirs(os.path.dirname(os.path.realpath(HASHES_FILENAME)), exist_ok=True)
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
        hashes = read_last_generated_templates_hashes()
        # collect all sources to generate
        generators = {}

        if hashes.is_template_changed("Weights.java.template"):
            for type in TYPE_ALL:
                gen = functools.partial(generate_weights, type)
                generators[weights_filename(type)] = gen

        if hashes.is_template_changed("IWeights.java.template"):
            for type in TYPE_ALL:
                gen = functools.partial(generate_iweights, type)
                generators[iweights_filename(type)] = gen

        if hashes.is_template_changed("WeightsImpl.java.template"):
            for type in TYPE_ALL:
                gen = functools.partial(generate_weights_impl, type)
                generators[weights_impl_filename(type)] = gen

        if hashes.is_template_changed("ReferenceableHeap.java.template"):
            types = []
            # for key_type in TYPE_ALL - {"Bool"}:
            #     for value_type in TYPE_ALL | {"Void"}:
            #         types.append((key_type, value_type))
            types.append(("Int", "Int"))
            types.append(("Int", "Void"))
            types.append(("Double", "Int"))
            types.append(("Double", "Obj"))
            types.append(("Obj", "Void"))
            types.append(("Obj", "Obj"))
            for key_type, value_type in types:
                gen = functools.partial(
                    generate_referenceable_heap, key_type, value_type
                )
                generators[referenceable_heap_filename(key_type, value_type)] = gen

        if hashes.is_template_changed("PairingHeap.java.template"):
            types = []
            types.append(("Int", "Int"))
            types.append(("Int", "Void"))
            types.append(("Double", "Int"))
            types.append(("Double", "Obj"))
            types.append(("Obj", "Void"))
            types.append(("Obj", "Obj"))
            for key_type, value_type in types:
                gen = functools.partial(generate_pairing_heap, key_type, value_type)
                generators[pairing_heap_filename(key_type, value_type)] = gen

        if hashes.is_template_changed("BinomialHeap.java.template"):
            types = []
            types.append(("Int", "Int"))
            types.append(("Int", "Void"))
            types.append(("Double", "Int"))
            types.append(("Double", "Obj"))
            types.append(("Obj", "Void"))
            for key_type, value_type in types:
                gen = functools.partial(generate_binomial_heap, key_type, value_type)
                generators[binomial_heap_filename(key_type, value_type)] = gen

        if hashes.is_template_changed("FibonacciHeap.java.template"):
            types = []
            types.append(("Int", "Int"))
            types.append(("Int", "Void"))
            types.append(("Double", "Int"))
            types.append(("Double", "Obj"))
            types.append(("Obj", "Void"))
            for key_type, value_type in types:
                gen = functools.partial(generate_fibonacci_heap, key_type, value_type)
                generators[fibonacci_heap_filename(key_type, value_type)] = gen

        if hashes.is_template_changed("BinarySearchTree.java.template"):
            types = []
            types.append(("Int", "Int"))
            types.append(("Int", "Void"))
            types.append(("Double", "Int"))
            types.append(("Double", "Obj"))
            types.append(("Obj", "Void"))
            types.append(("Obj", "Obj"))
            for key_type, value_type in types:
                gen = functools.partial(
                    generate_binary_search_tree, key_type, value_type
                )
                generators[binary_search_tree_filename(key_type, value_type)] = gen

        if hashes.is_template_changed("BinarySearchTrees.java.template"):
            types = []
            types.append("Int")
            types.append("Double")
            types.append("Obj")
            for key_type in types:
                gen = functools.partial(generate_binary_search_trees, key_type)
                generators[binary_search_trees_filename(key_type)] = gen

        if hashes.is_template_changed("RedBlackTree.java.template"):
            types = []
            types.append(("Int", "Int"))
            types.append(("Int", "Void"))
            types.append(("Double", "Int"))
            types.append(("Double", "Obj"))
            types.append(("Obj", "Void"))
            types.append(("Obj", "Obj"))
            for key_type, value_type in types:
                gen = functools.partial(generate_red_black_tree, key_type, value_type)
                generators[red_black_tree_filename(key_type, value_type)] = gen

        if hashes.is_template_changed("SplayTree.java.template"):
            types = []
            types.append(("Int", "Int"))
            types.append(("Int", "Void"))
            types.append(("Double", "Int"))
            types.append(("Double", "Obj"))
            types.append(("Obj", "Void"))
            types.append(("Obj", "Obj"))
            for key_type, value_type in types:
                gen = functools.partial(generate_splay_tree, key_type, value_type)
                generators[splay_tree_filename(key_type, value_type)] = gen

        if not generators:
            logging.info("No template changed, nothing to do.")
            return

        for _filename, generator in generators.items():
            generator()
        # format_sourcefiles(generators.keys())

        write_generated_templates()


if __name__ == "__main__":
    main()
