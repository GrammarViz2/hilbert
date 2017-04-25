#
# combine tracks 
# substitute the real time-stamps
# interpolate by distance
gpsbabel -D 3 -i gpx -f data_raw/GPSDATA1.gpx -x track,faketime=f20140201120001+10 -x interpolate,distance=0.0003k -o gpx -F tmp1.gpx
gpsbabel -D 3 -i gpx -f data_raw/GPSDATA2.gpx -x track,faketime=f20140302120001+10 -x interpolate,distance=0.0003k -o gpx -F tmp2.gpx
gpsbabel -D 3 -i gpx -f data_raw/GPSDATA3.gpx -x track,faketime=f20140403120001+10 -x interpolate,distance=0.0003k -o gpx -F tmp3.gpx
gpsbabel -D 3 -i gpx -f data_raw/GPSDATA4.gpx -x track,faketime=f20140504120001+10 -x interpolate,distance=0.0003k -o gpx -F tmp4.gpx
#
# combine all data into a single file
gpsbabel -D 3 -i gpx -f tmp1.gpx -i gpx -f tmp2.gpx -i gpx -f tmp3.gpx -i gpx -f tmp4.gpx -x track,pack,title="Anomaly" -o gpx -F tmp5.gpx
#
# resample time-stamps again
gpsbabel -D 3 -i gpx -f tmp5.gpx -x track,faketime=f20100201120001+10 -o gpx -F anomaly.gpx
#     
# prune consecutive points which are within two meters
# but take care about consecutive points within two minutes
gpsbabel -D 3 -i gpx -f anomaly.gpx -x position,distance=2m,time=120 -o gpx -F anomaly_pruned.gpx
#
# finally convert this to CSV so it becomes SAXSequitur and Hilbert digestible
gpsbabel -D 3 -i gpx -f anomaly_pruned.gpx -o xcsv,style=anomaly.style -F anomaly_pruned.csv
#
# application of Hilbert curve requires some manual intervention
#
# but then we can get the curve out
#
cut -d',' -f2 anomaly_pruned_hilbert_curve.csv >anomaly_pruned_hilbert_curve_4Sequitur.csv
#
# and search for anomalies for example
psenin@T135:~/git/hilbert/data/anomaly$ java -Xmx2G -cp "sequitur.jar" edu.hawaii.jmotif.discords.SAXSequiturDiscord 3 anomaly_pruned_hilbert_curve_4Sequitur.csv 350 15 4
#setParameters [INFO|main|11:30:10] Parsing param string "[3, anomaly_pruned_hilbert_curve_4Sequitur.csv, 350, 15, 4]" 
#loadData [FINE|main|11:30:10] reading from anomaly_pruned_hilbert_curve_4Sequitur.csv 
#loadData [INFO|main|11:30:11] loaded 17175 points from 17175 lines in anomaly_pruned_hilbert_curve_4Sequitur.csv 
#setParameters [INFO|main|11:30:11] Starting discords search with settings: algorithm 3, data "anomaly_pruned_hilbert_curve_4Sequitur.csv", window 350, PAA 15, alphabet 4 
#findSaxSequitur [INFO|main|11:30:11] running SAXSequitur algorithm... 
#findSaxSequitur [INFO|main|11:30:14] minimal rules density is 0, at 11769 
#findSaxSequitur [INFO|main|11:30:14] coverage-based discords, start-end-coverage intervals: 11769 - 11777, len 8, cov 0;  
#findSaxSequitur [INFO|main|11:30:14] *** Interval coverage-based discords, start-end-coverage intervals: 11509 - 11878, len 369, cov 2.585714285714286;  
#findSaxSequitur [INFO|main|11:30:14] starting SAXSequitur search ...  
#series2Discords [FINE|main|11:30:18] discord string "rule 140, length 366", position 4659, NN distance 294881.39809082565, elapsed time: 0h 0m 1s 736ms, distance calls: 160653 
#series2Discords [FINE|main|11:30:19] discord string "rule 166, length 387", position 6536, NN distance 246049.883308243, elapsed time: 0h 0m 1s 175ms, distance calls: 125759 
#series2Discords [FINE|main|11:30:21] discord string "rule 241, length 393", position 14170, NN distance 233189.01809690782, elapsed time: 0h 0m 1s 434ms, distance calls: 191780 
#series2Discords [FINE|main|11:30:22] discord string "rule 145, length 360", position 10987, NN distance 214766.3168120178, elapsed time: 0h 0m 1s 235ms, distance calls: 170051 
#series2Discords [FINE|main|11:30:24] discord string "rule 168, length 359", position 7055, NN distance 150713.14303669738, elapsed time: 0h 0m 1s 864ms, distance calls: 264393 
#discord "rule 140, length 366", at 4659 distance to closest neighbor: 294881.39809082565, info string: "discord string "rule 140, length 366", position 4659, NN distance 294881.39809082565, elapsed time: 0h 0m 1s 736ms, distance calls: 160653"
#
# some plotting
Rscript --no-save --no-restore plotting.R anomaly_pruned_hilbert_curve_4Sequitur.csv coverage.txt distances.txt
display SAXSequitur.png
#
#
# extract anomaly
sed -n 11509,11878p anomaly_pruned.csv >anomaly_00.csv
# and convert it into track
gpsbabel -D 3 -i xcsv,style=anomaly.style -f anomaly_00.csv -x track,pack,title="Anomaly00" -x transform,trk=wpt,del -o gpx -o gpx -F anomaly_00.gpx
#
#psenin@T135:~/git/hilbert/data/anomaly$ sed -n 4659,5025p anomaly_pruned.csv >anomaly_01.csv
#psenin@T135:~/git/hilbert/data/anomaly$ gpsbabel -D 3 -i xcsv,style=anomaly.style -f anomaly_01.csv -x track,pack,title="Anomaly01" -x transform,trk=wpt,del -o gpx -o gpx -F anomaly_01.gpx
#
#psenin@T135:~/git/hilbert/data/anomaly$ sed -n 6536,6923p anomaly_pruned.csv >anomaly_02.csv
#psenin@T135:~/git/hilbert/data/anomaly$ gpsbabel -D 3 -i xcsv,style=anomaly.style -f anomaly_02.csv -x track,pack,title="Anomaly02" -x transform,trk=wpt,del -o gpx -o gpx -F anomaly_02.gpx
sed -n 14170,14563p anomaly_pruned.csv >anomaly_03.csv
gpsbabel -D 3 -i xcsv,style=anomaly.style -f anomaly_03.csv -x track,pack,title="Anomaly03" -x transform,trk=wpt,del -o gpx -o gpx -F anomaly_03.gpx
sed -n 10987,11347p anomaly_pruned.csv >anomaly_04.csv
gpsbabel -D 3 -i xcsv,style=anomaly.style -f anomaly_04.csv -x track,pack,title="Anomaly04" -x transform,trk=wpt,del -o gpx -o gpx -F anomaly_04.gpx
sed -n 7055,7414p anomaly_pruned.csv >anomaly_05.csv
gpsbabel -D 3 -i xcsv,style=anomaly.style -f anomaly_05.csv -x track,pack,title="Anomaly05" -x transform,trk=wpt,del -o gpx -o gpx -F anomaly_05.gpx
