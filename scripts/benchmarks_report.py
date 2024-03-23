import argparse
import csv
import datetime
import json
import os
import tempfile
from datetime import datetime

import artifacts_utils
import matplotlib
import numpy as np
import requests
from matplotlib import pyplot as plt


def read_results_file(path):
	results = []
	with open(path) as csvfile:
		spamreader = csv.reader(csvfile)
		first_row = True
		columns = None
		for row in spamreader:
			if first_row:
				columns = row
				first_row = False
			else:
				res = {}
				for c in range(len(columns)):
					res[columns[c]] = row[c]
				results.append(res)
	return results


def create_report(out_img):
	artifacts = artifacts_utils.get_artifacts_description("jmh-benchmarks-results")
	with tempfile.TemporaryDirectory() as artifacts_dir:
		artifacts_utils.download_artifacts(artifacts, artifacts_dir)
		for id, artifact in artifacts.items():
			res_path = os.path.join(artifacts_dir, str(id), "bench_results", "bench_results.csv")
			artifact['benchmarks'] = read_results_file(res_path)
		benchmarks = dict()
		for id, artifact in artifacts.items():
			for bench in artifact['benchmarks']:
				bench_name = bench['Benchmark']
				bench_params = bench['Param: args']
				bench_key = (bench_name, bench_params)
				if bench_key not in benchmarks:
					benchmarks[bench_key] = list()
				bench['id'] = id
				benchmarks[bench_key].append(bench)

		imgs = []
		temp_img_file = "temp.png"
		try:
			for bench_key, results in benchmarks.items():
				print("analyzing benchmark", bench_key[0], bench_key[1])
				results = sorted(results, key=lambda r: artifacts[r['id']]['created_at'])
				scores = [float(res['Score']) for res in results]
				created_at = []
				for res in results:
					artifact = artifacts[res['id']]
					date = datetime.strptime(artifact['created_at'][0:10], '%Y-%m-%d')
					created_at.append(date)

				plt.gcf().suptitle(f"{bench_key[0]} {bench_key[1]}")
				plt.plot_date(matplotlib.dates.date2num(created_at), scores,'b-')
				plt.xticks(rotation=90)
				plt.savefig(temp_img_file)
				plt.clf()
				with open(temp_img_file, 'rb') as image_file:
					image = plt.imread(image_file)
				imgs.append(np.array(image))
		finally:
			if os.path.exists(temp_img_file):
				os.remove(temp_img_file)
		imgs = np.concatenate(imgs)
		print(f"Saving result image to {out_img}")
		matplotlib.pyplot.imsave(out_img, imgs)


def main():
	parser = argparse.ArgumentParser()
	parser.add_argument('--out-img', required=True)
	args = parser.parse_args()
	create_report(args.out_img)



if __name__ == "__main__":
	main()
