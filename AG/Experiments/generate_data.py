import itertools
import random
import re

input_file = '../Data/original_data.csv'
output_dir = 'D:/Temp/ExperimentData/'
num_data_points = 97 # 96 measurements + patient header line

def generate_file(input_file, num_patients):

	def grouper(iterable, n):
		args = [iter(iterable)] * n
		return itertools.zip_longest(*args)

	with open(input_file) as f:
		lines = f.readlines()
		data = [group for group in grouper(lines, num_data_points)]
		size = len(data)
		if num_patients < size:
			output_data = random.sample(data, num_patients)
		else:
			output_data = data
			num_to_add = num_patients - size
			while num_to_add > 0:
				if num_to_add <= size:
					output_data += random.sample(data, num_to_add)
					num_to_add -= num_to_add
				else:
					output_data += random.sample(data, size)
					num_to_add -= size
			print(len(output_data))
	
	output_file = output_dir + 'out_' + str(num_patients) + '_patients.csv'
	with open(output_file, 'w') as f:
		for line in output_data:
			f.write(''.join(line))

output_sizes = (10**exp for exp in range(2, 7))
for size in output_sizes:
	generate_file(input_file, size)

