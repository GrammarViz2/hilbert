 2745  history | grep anomaly
 2746  sed -n 1,10987p anomaly_pruned.csv >part1.csv
 2747  sed -n 10987,11347p anomaly_pruned.csv >part2.csv
 2748  gpsbabel -D 3 -i xcsv,style=anomaly.style -f part1.csv -x track,pack,title="Anomaly4" -x transform,trk=wpt,del -o gpx -o gpx -F part1.gpx
 2749  gpsbabel -D 3 -i xcsv,style=anomaly.style -f part2.csv -x track,pack,title="Anomaly4" -x transform,trk=wpt,del -o gpx -o gpx -F part2.gpx
 2750  gpsbabel -D 3 -i xcsv,style=anomaly.style -f anomaly_04.csv -x track,pack,title="Anomaly4" -x transform,trk=wpt -o gpx -o gpx -F anomaly_04.gpx
 2751  gpsbabel -D 3 -i xcsv,style=anomaly.style -f anomaly_03.csv -x track,pack,title="Anomaly3" -x transform,trk=wpt -o gpx -o gpx -F anomaly_03.gpx
 2752  gpsbabel -D 3 -i xcsv,style=anomaly.style -f anomaly_01.csv -x track,pack,title="Anomaly01" -x transform,trk=wpt -o gpx -o gpx -F anomaly_01.gpx
 2753  sed -n 1,4659p anomaly_pruned.csv >part1.csv
 2754  wc -l anomaly_pruned.csv
 2755  sed -n 1,10987p anomaly_pruned.csv >part1.csv
 2756  sed -n 10987,17176p anomaly_pruned.csv >part2.csv
 2757  gpsbabel -D 3 -i xcsv,style=anomaly.style -f part1.csv -x track,pack,title="Anomaly4" -x transform,trk=wpt,del -o gpx -o gpx -F part1.gpx
 2758  gpsbabel -i xcsv,style=anomaly.style -f part2.csv -x track,pack,title="Anomaly4" -x transform,trk=wpt,del -o gpx -o gpx -F part2.gpx
 2759  sed -n 1,4659p anomaly_pruned.csv >part11.csv
 2760  sed -n 5025,17176p anomaly_pruned.csv >part2.csv
 2761  gpsbabel -i xcsv,style=anomaly.style -f part2.csv -x track,pack,title="Anomaly4" -x transform,trk=wpt,del -o gpx -o gpx -F part2.gpx
 2762  gpsbabel -i xcsv,style=anomaly.style -f part11.csv -x track,pack,title="Anomaly4" -x transform,trk=wpt,del -o gpx -o gpx -F part11.gpx
 2763  sed -n 4659,5025p anomaly_pruned.csv >anomaly_01.csv
 2764  gpsbabel -D 3 -i xcsv,style=anomaly.style -f anomaly_01.csv -x track,pack,title="Anomaly01" -x transform,trk=wpt -o gpx -o gpx -F anomaly_01.gpx
 2765  history | grep anomaly

