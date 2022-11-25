#!/usr/bin/env python3

from os import listdir
from os.path import isfile, join
import pandas as pd
import matplotlib.pyplot as plt


path="results/logmaplite/"
dirfiles = [f for f in listdir(path) if isfile(join(path, f))]

for file in dirfiles:
	df = pd.read_csv(path+file, header=None)
	x = df[df.columns[1]]
	y = df[df.columns[0]]

	plt.figure()
	plt.plot(x, y, 'o-r', label = "Client 1")

	for a,b in zip(x,y):
		plt.text(a,b,str(b), rotation=45)

	plt.xticks(rotation = 90)
	plt.xlabel("Client")
	plt.ylabel("F-Score")
	plt.grid(True)
	plt.legend()

	file = file.split(".")
	plt.savefig(path+"graphs/"+file[0]+".png", bbox_inches='tight')
    