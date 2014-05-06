#  
#  sed new_stops_with_possible_matches.osm -e 's/node/sdsd/'
#  #grep -e \"gtfs_id\" -e id='new_stops_with_possible_matches.osm 
# 
# 
# grep -P [\'\"]gtfs_id[\'\"]  new_stops_with_possible_matches.osm 
# 
# grep -P \<node new_stops_with_possible_matches.osm 
# 
# 
# grep -P [\'\"]gtfs_id[\'\"]-P \<node new_stops_with_possible_matches.osm 
# 
# grep -P \<node new_stops_with_possible_matches.osm 
# 
# 
# grep -e \"gtfs_id new_stops_with_possible_matches.osm

#grep -e [\'\"]gtfs_id -e id=[\'\"] new_stops_with_possible_matches.osc

#=if(A1=A2,"TRUE","")


#grep -e [\'\"]gtfs_id -e id=[\'\"] new_stops_with_possible_matches.osc | sed "s/.*\id=['\"]\([0-9]*\).*/\1/"| 
#grep -e [\'\"]gtfs_id -e id=[\'\"] new_stops_with_possible_matches.osc | sed "s/.*\id=['\"]\([0-9]*\).*/\1/"| sed "s/\n.*\v=['\"]\([0-9]*\).*/\1/" | sed ':a;N;$!ba;s/\n</,/g' 

grep -e [\'\"]gtfs_id -e id=[\'\"] new_stops_with_possible_matches.osc | sed "s/.*\id=['\"]\([0-9A-Za-z_]*\).*/\1/"| sed "s/.*v=['\"]\([0-9A-Za-z_]*\).*/,\1/"| sed ':a;N;$!ba;s/\n,/,/g'
# grep -e [\'\"]gtfs_id -e id=[\'\"] "DUMMY_OSM_CHANGE to 10093.xml" | sed "s/.*\id=['\"]\([0-9]*\).*/\1/"| sed "s/.*v=['\"]\([0-9]*\).*/,\1/"| sed ':a;N;$!ba;s/\n,/,/g'  
