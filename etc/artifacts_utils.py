# breaking grequests breaks requests (https://github.com/spyoungtech/grequests/issues/103), workaround:
from pathlib import Path

from gevent import monkey


def stub(*args, **kwargs):  # pylint: disable=unused-argument
    pass


monkey.patch_all = stub
import json
import shutil
import tarfile
import zipfile

import grequests
import requests


def get_artifacts_description_all() -> dict[int, dict]:
    url = "https://api.github.com/repos/barakugav/JGAlgo/actions/artifacts"
    headers = {
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
    }
    resp = requests.get(url, headers=headers)
    if not resp.ok:
        raise ValueError(resp)
    artifacts = json.loads(resp.content)["artifacts"]
    artifacts = {a["id"]: a for a in artifacts}
    return artifacts


def get_artifacts_description(name: str) -> dict[int, dict]:
    artifacts = {}
    for id, artifact in get_artifacts_description_all().items():
        if artifact["name"] == name:
            artifacts[id] = artifact
    return artifacts


def download_artifacts(artifacts: dict[int, dict], outdir: Path):
    urls = []
    for id, artifact in artifacts.items():
        url: str = artifact["archive_download_url"]
        assert url.startswith("https://api.github.com/repos")
        assert url.endswith("/zip")
        url = url.replace("https://api.github.com/repos", "https://nightly.link")
        url = url.replace("/zip", ".zip")
        urls.append(url)
    print("downloading artifacts...")
    for url in urls:
        print(f"\t{url}")
    rs = (grequests.get(u) for u in urls)
    rs = grequests.map(rs)

    for (id, artifact), resp in zip(artifacts.items(), rs):
        zip_path = outdir / f"{id}.zip"
        with open(zip_path, "wb") as fd:
            chunk_size = 128
            for chunk in resp.iter_content(chunk_size=chunk_size):
                fd.write(chunk)

        dir_path_temp = outdir / f"{id}temp"
        with zipfile.ZipFile(zip_path, "r") as zip_ref:
            zip_ref.extractall(dir_path_temp)
        zip_path.unlink()
        tar_path = dir_path_temp / "bench_results.tar.gz"

        dir_path = outdir / str(id)
        with tarfile.open(tar_path, "r:gz") as tar:
            tar.extractall(dir_path)
        shutil.rmtree(dir_path_temp)

    print("all artifacts downloaded successfully")
