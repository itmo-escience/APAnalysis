with open("../Data/original_data.csv", "r") as f:
    data = f.readlines()

single_line_data = "\t".join(data)

with open("./minified_data.txt", "w") as f:
    f.write(single_line_data)