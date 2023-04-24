import os
import subprocess

SCRIPTS_DIR = os.path.dirname(os.path.realpath(__file__))
TOP_DOR = os.path.abspath(os.path.join(SCRIPTS_DIR, ".."))


def main():
    def run_cmd(cmd):
        subprocess.check_call(cmd, cwd=TOP_DOR, shell=True)

    print("\n\n ============ Clean ============\n")
    run_cmd("mvn clean")

    print("\n\n ============ Build ============\n")
    run_cmd("mvn --batch-mode --update-snapshots package -Dmaven.test.skip")

    print("\n\n ============ Tests ============\n")
    run_cmd("mvn --batch-mode test -DfailIfNoTests=false")

    print("\n\n ============ SpotBugs ============\n")
    run_cmd("mvn --batch-mode spotbugs:check -pl jgalgo-core")

    print("\n\n ============ Javadoc ============\n")
    run_cmd("mvn javadoc:aggregate -pl jgalgo-core")

    print("\nPre-commit check passed successfully")


if __name__ == "__main__":
    main()
