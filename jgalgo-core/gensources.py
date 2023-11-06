import os
import sys
import shutil
import subprocess

TOP_DIR = os.path.dirname(os.path.realpath(__file__))
TEMPLATE_DIR = os.path.join(TOP_DIR, "template")
SOURCE_DIR = os.path.join(TOP_DIR, "src", "main", "java")
PACKAGE_DIR = os.path.join(SOURCE_DIR, "com", "jgalgo")
TYPE_ALL = ["Obj", "Byte", "Short", "Int", "Long", "Float", "Double", "Bool", "Char"]


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
    print("Formatting generated files...")
    ECLIPSE_PATH = find_eclipse()
    if ECLIPSE_PATH is None:
        print("Failed to find eclipse.")
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
    print("Generating " + output_filename + " from " + input_filename)

    def apply_function(text, func_name, func):
        begin = 0
        while True:
            begin = text.find(func_name, begin)
            if begin == -1:
                return text
            words, end = find_func_args(text, begin + len(func_name))
            text = text[:begin] + func(*words) + text[end:]

    text = open(input_filename).read()

    # Replace all constants one by one, in reverse sorted order to (hopefully) avoid one constant being a prefix of another
    for constant, value in sorted(
        constants.items(), key=lambda kv: kv[0], reverse=True
    ):
        text = text.replace(constant, value)

    # Replace all functions calls one by one, in reverse sorted order to (hopefully) avoid one constant being a prefix of another
    for func_name, func in sorted(
        functions.items(), key=lambda kv: kv[0], reverse=True
    ):
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


def get_constants_and_functions(type):
    constants = {
        "Obj": {
            "TYPE_NAME": "Obj",
            "PRIMITIVE_TYPE": "T",
            "PRIMITIVE_TYPE_REAL": "Object",
            "TYPE_GENERIC_CLASS": "T",
            "FASTUTIL_TYPE": "Object",
            "FASTUTIL_PACKAGE": "it.unimi.dsi.fastutil.objects",
        },
        "Byte": {
            "TYPE_NAME": "Byte",
            "PRIMITIVE_TYPE": "byte",
            "PRIMITIVE_TYPE_REAL": "byte",
            "TYPE_GENERIC_CLASS": "Byte",
            "FASTUTIL_TYPE": "Byte",
            "FASTUTIL_PACKAGE": "it.unimi.dsi.fastutil.bytes",
        },
        "Short": {
            "TYPE_NAME": "Short",
            "PRIMITIVE_TYPE": "short",
            "PRIMITIVE_TYPE_REAL": "short",
            "TYPE_GENERIC_CLASS": "Short",
            "FASTUTIL_TYPE": "Short",
            "FASTUTIL_PACKAGE": "it.unimi.dsi.fastutil.shorts",
        },
        "Int": {
            "TYPE_NAME": "Int",
            "PRIMITIVE_TYPE": "int",
            "PRIMITIVE_TYPE_REAL": "int",
            "TYPE_GENERIC_CLASS": "Integer",
            "FASTUTIL_TYPE": "Int",
            "FASTUTIL_PACKAGE": "it.unimi.dsi.fastutil.ints",
        },
        "Long": {
            "TYPE_NAME": "Long",
            "PRIMITIVE_TYPE": "long",
            "PRIMITIVE_TYPE_REAL": "long",
            "TYPE_GENERIC_CLASS": "Long",
            "FASTUTIL_TYPE": "Long",
            "FASTUTIL_PACKAGE": "it.unimi.dsi.fastutil.longs",
        },
        "Float": {
            "TYPE_NAME": "Float",
            "PRIMITIVE_TYPE": "float",
            "PRIMITIVE_TYPE_REAL": "float",
            "TYPE_GENERIC_CLASS": "Float",
            "FASTUTIL_TYPE": "Float",
            "FASTUTIL_PACKAGE": "it.unimi.dsi.fastutil.floats",
        },
        "Double": {
            "TYPE_NAME": "Double",
            "PRIMITIVE_TYPE": "double",
            "PRIMITIVE_TYPE_REAL": "double",
            "TYPE_GENERIC_CLASS": "Double",
            "FASTUTIL_TYPE": "Double",
            "FASTUTIL_PACKAGE": "it.unimi.dsi.fastutil.doubles",
        },
        "Bool": {
            "TYPE_NAME": "Bool",
            "PRIMITIVE_TYPE": "boolean",
            "PRIMITIVE_TYPE_REAL": "boolean",
            "TYPE_GENERIC_CLASS": "Boolean",
            "FASTUTIL_TYPE": "Boolean",
            "FASTUTIL_PACKAGE": "it.unimi.dsi.fastutil.booleans",
        },
        "Char": {
            "TYPE_NAME": "Char",
            "PRIMITIVE_TYPE": "char",
            "PRIMITIVE_TYPE_REAL": "char",
            "TYPE_GENERIC_CLASS": "Character",
            "FASTUTIL_TYPE": "Char",
            "FASTUTIL_PACKAGE": "it.unimi.dsi.fastutil.chars",
        },
    }[type]

    if type == "Obj":
        constants["TYPE_GENERIC"] = "<T>"
        constants["TYPE_GENERIC_IN_TEMPLATE_LIST"] = ", T"
        constants["CAST_TO_GENERIC"] = "(T)"
        constants["SUPPRESS_WARNINGS_UNCHECKED"] = '@SuppressWarnings("unchecked")'
    else:
        constants["TYPE_GENERIC"] = ""
        constants["TYPE_GENERIC_IN_TEMPLATE_LIST"] = ""
        constants["CAST_TO_GENERIC"] = ""
        constants["SUPPRESS_WARNINGS_UNCHECKED"] = ""

    functions = {
        "Obj": {
            "PRIMITIVE_TO_BOXED": lambda x: x,
            "BOXED_TO_PRIMITIVE": lambda x: x,
        },
        "Byte": {
            "PRIMITIVE_TO_BOXED": lambda x: "Byte.valueOf(" + x + ")",
            "BOXED_TO_PRIMITIVE": lambda x: x + ".byteValue()",
        },
        "Short": {
            "PRIMITIVE_TO_BOXED": lambda x: "Short.valueOf(" + x + ")",
            "BOXED_TO_PRIMITIVE": lambda x: x + ".shortValue()",
        },
        "Int": {
            "PRIMITIVE_TO_BOXED": lambda x: "Integer.valueOf(" + x + ")",
            "BOXED_TO_PRIMITIVE": lambda x: x + ".intValue()",
        },
        "Long": {
            "PRIMITIVE_TO_BOXED": lambda x: "Long.valueOf(" + x + ")",
            "BOXED_TO_PRIMITIVE": lambda x: x + ".longValue()",
        },
        "Float": {
            "PRIMITIVE_TO_BOXED": lambda x: "Float.valueOf(" + x + ")",
            "BOXED_TO_PRIMITIVE": lambda x: x + ".floatValue()",
        },
        "Double": {
            "PRIMITIVE_TO_BOXED": lambda x: "Double.valueOf(" + x + ")",
            "BOXED_TO_PRIMITIVE": lambda x: x + ".doubleValue()",
        },
        "Bool": {
            "PRIMITIVE_TO_BOXED": lambda x: "Boolean.valueOf(" + x + ")",
            "BOXED_TO_PRIMITIVE": lambda x: x + ".booleanValue()",
        },
        "Char": {
            "PRIMITIVE_TO_BOXED": lambda x: "Character.valueOf(" + x + ")",
            "BOXED_TO_PRIMITIVE": lambda x: x + ".charValue()",
        },
    }[type]

    return constants, functions


def weights_filename(type):
    return os.path.join(PACKAGE_DIR, "graph", "Weights" + type + ".java")


def iweights_filename(type):
    return os.path.join(PACKAGE_DIR, "graph", "IWeights" + type + ".java")


def weights_impl_filename(type):
    return os.path.join(PACKAGE_DIR, "graph", "WeightsImpl" + type + ".java")


def generate_weights(type):
    constants, functions = get_constants_and_functions(type)
    constants["IWEIGHTS"] = "IWeights" + type
    constants["WEIGHTS"] = "Weights" + type

    if type in ["Byte", "Short", "Int"]:
        constants["WEIGHT_FUNC_IMPLEMENT"] = ", WeightFunctionInt<K>"
        constants[
            "WEIGHT_FUNC_IMPLEMENTATION"
        ] = """
	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Implement the {@link WeightFunctionInt} interface by using the weights of the container.
	 */
	@Override
	default int weightInt(K element) {
		return get(element);
	}
"""
    elif type in ["Long", "Float", "Double"]:
        constants["WEIGHT_FUNC_IMPLEMENT"] = ", WeightFunction<K>"
        constants[
            "WEIGHT_FUNC_IMPLEMENTATION"
        ] = """
	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Implement the {@link WeightFunction} interface by using the weights of the container.
	 */
	@Override
	default double weight(K element) {
		return get(element);
	}
"""
    else:
        constants["WEIGHT_FUNC_IMPLEMENT"] = ""
        constants["WEIGHT_FUNC_IMPLEMENTATION"] = ""

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

    if type in ["Byte", "Short", "Int"]:
        constants["WEIGHT_FUNC_IMPLEMENT"] = ", IWeightFunctionInt"
        constants[
            "WEIGHT_FUNC_IMPLEMENTATION"
        ] = """
	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Implement the {@link IWeightFunctionInt} interface by using the weights of the container.
	 */
	@Override
	default int weightInt(int element) {
		return get(element);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Implement the {@link WeightFunctionInt} interface by using the weights of the container.
	 */
	@Deprecated
	@Override
	default int weightInt(Integer element) {
		return get(element.intValue());
	}

	@Deprecated
	@Override
	default int compare(Integer e1, Integer e2) {
		return IWeightFunctionInt.super.compare(e1.intValue(), e2.intValue());
	}
"""
    elif type in ["Long", "Float", "Double"]:
        constants["WEIGHT_FUNC_IMPLEMENT"] = ", IWeightFunction"
        constants[
            "WEIGHT_FUNC_IMPLEMENTATION"
        ] = """
	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Implement the {@link IWeightFunction} interface by using the weights of the container.
	 */
	@Override
	default double weight(int element) {
		return get(element);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Implement the {@link WeightFunctionInt} interface by using the weights of the container.
	 */
	@Deprecated
	@Override
	default double weight(Integer element) {
		return get(element.intValue());
	}

	@Deprecated
	@Override
	default int compare(Integer e1, Integer e2) {
		return IWeightFunction.super.compare(e1.intValue(), e2.intValue());
	}
"""
    else:
        constants["WEIGHT_FUNC_IMPLEMENT"] = ""
        constants["WEIGHT_FUNC_IMPLEMENTATION"] = ""

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


def all_generated_filenames():
    generated_filenames = []
    for type in TYPE_ALL:
        generated_filenames.append(weights_filename(type))
    for type in TYPE_ALL:
        generated_filenames.append(iweights_filename(type))
    for type in TYPE_ALL:
        generated_filenames.append(weights_impl_filename(type))
    return generated_filenames


def generate_all():
    for type in TYPE_ALL:
        generate_weights(type)
    for type in TYPE_ALL:
        generate_iweights(type)
    for type in TYPE_ALL:
        generate_weights_impl(type)
    format_sourcefiles(all_generated_filenames())


def clean():
    for filename in all_generated_filenames():
        if os.path.exists(filename):
            # print("Removing " + filename)
            os.remove(filename)


def main():
    clean()
    generate_all()


if __name__ == "__main__":
    main()
