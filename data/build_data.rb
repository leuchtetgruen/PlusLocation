#TODO make script that uses this script and https://download.bbbike.org/osm/bbbike/ to autogenerate csvs on server
require 'plus_codes/open_location_code'
require 'csv'
require 'pry'

if ARGV.size < 2
  puts "usage : ruby build_data.rb IN_FILE OUT_FILE"
  exit
end
IN_FILE = ARGV[0]
OUT_FILE = ARGV[1]

NEARBY_RELEVANT_TYPES = [139, 148, 160, 166, 167 ]

PREFIXES = {
  "167" =>  "Bhf ",
  "168" => "Tram ",
  "161" => "Bus ",
  "166" => "U "

}
OLC_CONVERTER = PlusCodes::OpenLocationCode.new

def add_prefix(line)
  title = if PREFIXES.keys.include?(line[0].to_s)
            pref = PREFIXES[line[0].to_s]
            if line[3].include?(pref)
              line[3]
            else
              pref + line[3]
            end
  else
    line[3]
  end
  [line[0], line[1], line[2], title]
end

def convert_to_olc(line, nearby)
  #line[0] is just there for debugging
  [OLC_CONVERTER.encode(line[1], line[2]), line[3], nearby ? 1 : 0, line[0]]
end

def convert(line, nearby)
  prefix_line = add_prefix(line)
  convert_to_olc(prefix_line, nearby)
end

csv_tmp_out_file = IN_FILE + ".csv"

if !File.exist?(csv_tmp_out_file)
  puts "Converting pbf file..."
  system("java -jar osmpois.jar -of #{csv_tmp_out_file} #{IN_FILE}")
end


puts "Reading CSV file..."
# read linewise
csv = []
File.open(csv_tmp_out_file).each do |line|
  arr = begin
          CSV.parse_line(line, {col_sep: '|'})
        rescue
          nil
        end
  csv << arr
end

puts "Converting..."
#remove faulty lines
csv.compact!

# remove unneccessary field and convert to right datatypes
csv.map! { |line|
  [line[0].to_i, line[2].to_f, line[3].to_f, line[4]]
}

# remove entries that have the same name
csv.uniq! { |line|
  if line[0].nil? or line[3].nil?
    nil
  else
    line[0].to_s + line[3]
  end
 }

target_csv = csv.map { |line|
  convert(line, NEARBY_RELEVANT_TYPES.include?(line[0]))  
}

target_csv_str = CSV.generate do |tcsv|
  target_csv.each { |line| tcsv << line }
end

puts "Writing to #{OUT_FILE}..."
File.write(OUT_FILE, target_csv_str)
