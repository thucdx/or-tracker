import pandas as pd
import argparse
from datetime import datetime

date_format = '%Y-%m-%d %H:%M:%S'

def parse_report_gps(inp):
    trip_data = pd.read_csv(inp)

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
    output_data['h_positionTime'] = output_data['positionTime'].apply(lambda ts: datetime.fromtimestamp(ts).strftime(date_format))

    output_data.reset_index(inplace=True)
    output_data['h_receiveTime'] = output_data['ts'].apply(
        lambda ts: datetime.fromtimestamp(ts/1000).strftime(date_format))

    return output_data


def parse_report_vehicle(inp):
    rv = pd.read_csv(inp)
    
    pivot = pd.pivot_table(rv, index=['ts'], columns='key', values=['long_v'])
    prefix = 'publishData.dataFlow.gpsPosition'
    pivot.columns = [col[-1].replace("publishData.dataFlow.gpsPosition.", "").replace("publishData.dataFlow.", "") for col in pivot.columns]
    pivot.reset_index(inplace=True)
    pivot['lat'] = pivot['lat'].apply(lambda u: u/1e5)
    pivot['lng'] = pivot['lng'].apply(lambda u: u/1e5)
    pivot['height'] = pivot['height'] - 10000
    pivot['h_positionTime'] = pivot['positionTime'].apply(lambda ts: datetime.fromtimestamp(ts).strftime(date_format))
    pivot['h_receiveTime'] = pivot['ts'].apply(lambda ts: datetime.fromtimestamp(ts/1000).strftime(date_format))
    
    return pivot

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Read trip")
    parser.add_argument("--mode", type=str, help="Mode: gps/vehicle. gps: GPS file, vehicle: report_vehicle report")
    parser.add_argument("--input", type=str, help="Path to input file")
    parser.add_argument("--output", type=str, help="Path to ouput file")

    args = parser.parse_args()
    
    if args.mode != None and args.input != None and args.output != None:
        if args.mode == "gps":
            f = parse_report_gps
        elif args.mode == "vehicle":
            f = parse_report_vehicle
        else:
            parser.print_help()
            exit(0)
                        
        converted_data = f(args.input)
        print(converted_data.head())
        converted_data.to_csv(args.output, index=False)
    else:
        parser.print_help()
                    