import argparse
import subprocess
from pathlib import Path

SCRIPTS_DIR = Path(__file__).parent.resolve()
TOP_DIR = SCRIPTS_DIR.parent.resolve()


def main(args):
    def run_cmd(cmd):
        subprocess.check_call(cmd, cwd=TOP_DIR, shell=True)

    print("\n\n ============ Clean and Build ============\n")
    if not args.skip_rebuild:
        run_cmd("mvn clean")
        run_cmd("mvn package -Dmaven.test.skip")
    else:
        print("skipping...")

    print("\n\n ============ Tests ============\n")
    if not args.skip_tests:
        run_cmd("mvn test jacoco:report")
    else:
        print("skipping...")

    print("\n\n ============ SpotBugs ============\n")
    if not args.skip_static:
        run_cmd("mvn compile spotbugs:check -pl -jgalgo-bench")
    else:
        print("skipping...")

    print("\n\n ============ Checkstyle ============\n")
    if not args.skip_style:
        run_cmd("mvn compile checkstyle:check")
    else:
        print("skipping...")

    print("\n\n ============ Javadoc ============\n")
    if not args.skip_javadoc:
        run_cmd("mvn javadoc:aggregate")
    else:
        print("skipping...")

    print("\nPre-commit check passed successfully")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(prog="Pre-Commit Check")
    parser.add_argument(
        "--skip-rebuild", action="store_true", help="skip clean and build"
    )
    parser.add_argument("--skip-tests", action="store_true", help="skip all unit tests")
    parser.add_argument(
        "--skip-static", action="store_true", help="skip static analysis"
    )
    parser.add_argument(
        "--skip-style", action="store_true", help="skip Checkstyle checks"
    )
    parser.add_argument(
        "--skip-javadoc", action="store_true", help="skip Javadoc generation"
    )
    args = parser.parse_args()
    main(args)
