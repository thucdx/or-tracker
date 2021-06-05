"""
Script to read trip
"""

import pandas as pd
import argparse
from datetime import datetime


def convert_data(input_file):
    trip_data = pd.read_csv(input_file)

    new_row = []
    for _, row in trip_data.iterrows():
        row['index_record'] = row['key'].split('[')[-1].split(']')[0]
        row['type'] = row['key'].split('.')[-1]
        new_row.append(row)

    new_trip_data = pd.DataFrame(new_row)
    output_data = pd.pivot_table(new_trip_data,
                                 index=['ts', 'index_record'],
                                 columns=['type'],
                                 values=['long_v'])
    output_data.columns = [col[-1] for col in output_data.columns]
    output_data.astype({'lat': 'float', 'lng': 'float'}, copy=False)
    output_data['lat'] = output_data['lat'].apply(lambda u: u/1e5)
    output_data['lng'] = output_data['lng'].apply(lambda u: u/1e5)
    output_data['h_positionTime'] = output_data['positionTime'].apply(lambda ts: datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S'))

    output_data.reset_index(inplace=True)
    output_data['h_receiveTime'] = output_data['ts'].apply(
        lambda ts: datetime.fromtimestamp(ts/1000).strftime('%Y-%m-%d %H:%M:%S'))

    return output_data

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Read trip")
    parser.add_argument("--input_fp", type=str, help="Path to input file")
    parser.add_argument("--output_fp", type=str, help="Path to ouput file")

    args = parser.parse_args()

    converted_data = convert_data(args.input_fp)
    print(converted_data.head())
    converted_data.to_csv(args.output_fp, index=False)