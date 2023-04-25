import os
import subprocess
import argparse


SCRIPTS_DIR = os.path.dirname(os.path.realpath(__file__))
TOP_DOR = os.path.abspath(os.path.join(SCRIPTS_DIR, ".."))


def main(args):
    def run_cmd(cmd):
        subprocess.check_call(cmd, cwd=TOP_DOR, shell=True)

    if not args.skip_rebuild:
        print("\n\n ============ Clean ============\n")
        run_cmd("mvn clean")
        print("\n\n ============ Build ============\n")
        run_cmd("mvn --batch-mode --update-snapshots package -Dmaven.test.skip")

    print("\n\n ============ Tests ============\n")
    if not args.skip_tests:
        run_cmd("mvn --batch-mode test -DfailIfNoTests=false")
    else:
        print("skipping...")

    print("\n\n ============ SpotBugs ============\n")
    if not args.skip_static:
        run_cmd("mvn --batch-mode spotbugs:check -pl jgalgo-core")
    else:
        print("skipping...")

    print("\n\n ============ Javadoc ============\n")
    if not args.skip_javadoc:
        run_cmd("mvn javadoc:aggregate -pl jgalgo-core")
    else:
        print("skipping...")

    print("\nPre-commit check passed successfully")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(prog='ProgramName')
    parser.add_argument('--skip-rebuild', action='store_true',  help='skip clean and build')
    parser.add_argument('--skip-tests', action='store_true',  help='skip all unit tests')
    parser.add_argument('--skip-static', action='store_true',  help='skip static analysis')
    parser.add_argument('--skip-javadoc', action='store_true',  help='skip Javadoc generation')
    args = parser.parse_args()
    main(args)
