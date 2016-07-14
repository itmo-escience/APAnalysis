import functools

import itertools
import os

import math

input_file = '../Data/original_data.csv'
output_dir = '../Resource/'

INTEGER = "Integer"
STRING = "String"
TIMESTAMP = "Timestamp"
BOOLEAN = "Boolean"

PEOPLE_FORMAT = (INTEGER, INTEGER, STRING, STRING, STRING, STRING, TIMESTAMP, BOOLEAN, TIMESTAMP, TIMESTAMP)
TONOMETRY_DATUM_FORMAT = (INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, TIMESTAMP, TIMESTAMP)

STR_SAMPLE = "".join(("s" for _ in range(0, 650)))


def sample_gen(format_str):
    def type_to_val(value_type):
        if value_type == INTEGER:
            #1 byte per symbol
            return "iiii"
        if value_type == STRING:
            return STR_SAMPLE
        if value_type == TIMESTAMP:
            return "dddddddd"
        if value_type == BOOLEAN:
            return "x"
        raise Exception("Wrong value type identifier {0}".format(value_type))

    return "".join((type_to_val(value_type)
                    for value_type in format_str))


def get_patient_sample(input_file):
    with open(input_file, "r") as f:
        pdata = f.readlines()

    if not pdata[0].startswith("PATIENT"):
        raise Exception("Invalid file format")

    measurements = itertools.takewhile(lambda x: not x.startswith("PATIENT"), pdata[1:])

    return pdata[:1] + list(measurements)


def generate_singlelin_file(one_patient_sample, count, name="patients", aligning=False):
    block_record_count = 10000
    aligner = ""

    if aligning:
        if not os.path.exists(os.path.join(output_dir, "{0}_10000.txt".format(name))):
            print("Generating  ideal file for block...")
            generate_singlelin_file(one_patient_sample, count=block_record_count, name=name, aligning=False)

        block_size = os.path.getsize(os.path.join(output_dir, "{0}_10000.txt".format(name)))

        if block_size % 512 != 0:
            diff_bsize = math.ceil(block_size / 512) * 512 - block_size
            if diff_bsize % 2 != 0:
                raise Exception("This situation actually has not to happen at all")
            aligner = "".join(("x" for _ in range(int(diff_bsize))))

        print("Block size {0}".format(block_size + len(aligner)))

    filepath = os.path.join(output_dir, "{0}_{1}.txt").format(name, count)
    singleline = "\t\t" + "\t".join(one_patient_sample)
    """
    in order to fix
    -put: Invalid values: dfs.bytes-per-checksum (=512) must divide block size (=33760000).

    """
    block = "".join((singleline for i in range(block_record_count))) + aligner + "\n"
    with open(filepath, "w") as f:
         for _ in range(0, int(count / block_record_count)):
             f.write(block)
    pass


if __name__ == "__main__":
    PEOPLE_SAMPLE = sample_gen(PEOPLE_FORMAT)
    TONOMETRY_DATUM_SAMPLE = sample_gen(TONOMETRY_DATUM_FORMAT)

    #one_patient_sample = get_patient_sample(input_file)

    for count in range(100000, 1000001, 100000):
        print("Generate sample for {0}".format(count))
        generate_singlelin_file(PEOPLE_SAMPLE, count=count, name="people", aligning=True)

    # generate_singlelin_file(one_patient_sample, count=100000)
    # generate_singlelin_file(one_patient_sample, count=1000000)