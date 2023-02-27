#!/usr/bin/env python3

from os import listdir
from os.path import isfile, join
import pandas as pd
import matplotlib.pyplot as plt
import sys


path="results/" + sys.argv[1] + "/"
dirfiles = [f for f in listdir(path) if isfile(join(path, f))]

for file in dirfiles:
	df = pd.read_csv(path+file, header=None)
	x = list(range(0, df.shape[0], 1))
	y1 = df[df.columns[0]]
	y2 = df[df.columns[1]]

	plt.figure()
	plt.plot(x, y1, 'o-r', label = "Client 1")
	plt.plot(x, y2, 'o-b', label = "Client 2")

	for a,b in zip(x,y1):
		plt.text(a,b,str(b))
	for a,b in zip(x,y2):
		plt.text(a,b,str(b))
	
	plt.xlabel("Iteration")
	plt.ylabel("F-Score")
	plt.grid(True)
	plt.legend()

	file = file.split(".")
	plt.savefig(path+"graphs/"+file[0]+".png", bbox_inches='tight')
    