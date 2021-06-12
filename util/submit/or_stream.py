__author__ = "thucdx"
__copyright__ = "Copyright 2021 Viettel"

import json
import math
import optparse
import pandas as pd
import requests
import time

parser = optparse.OptionParser("or_stream.py [options]")
parser.add_option("--url", dest="url", help="Tracking webservice URL", default="http://localhost:1234/track")
parser.add_option("--file", dest="file", help="CSV file with sample data.", default="samples/atuanhm_merge_2705.csv")
parser.add_option("--id", dest="id", help="Object id.", default="Sample car")
parser.add_option("--step", action="store_true", dest="step", default=False, help="Send stepwise.")
parser.add_option("--delay", default=0.001, help="Delay between messages")

(options, args) = parser.parse_args()

if options.file is None or options.url is None:
    parser.print_help()
    exit(1)

inp = pd.read_csv(options.file)
prev = None

for rid in range(inp.shape[0]):
    print(rid)
    row = inp.loc[rid]
    obj = {
        "id": options.id,
        "time": row['positionTime'] * 1000,
        "lat": row['lat'],
        "lng": row['lng'],
        "azimuth": row['heading']
    }

    if row['speed'] is not None and not math.isnan(row['speed']):
        obj['velocity'] = row['speed']

    json_str = json.dumps(obj)
    if options.step == True:
        raw_input("Press enter to continue...")
    elif prev != None:
        time.sleep(options.delay)

    prev = json_str
    print json_str
    x = requests.post(options.url, data=json_str)
    print(x.text)

