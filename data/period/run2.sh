#
# fake time from part1 and interpolate by distance
gpsbabel -D 3 -i gpx -f loops/GPSDATA1.gpx -x discard,fixnone -x interpolate,distance=0.001k -x track,faketime=f20100201120001+60 -o gpx -F tmp1.gpx
gpsbabel -D 3 -i gpx -f loops/GPSDATA2.gpx -x discard,fixnone -x interpolate,distance=0.001k -x track,faketime=f20110201120001+60 -o gpx -F tmp2.gpx
gpsbabel -D 3 -i gpx -f loops/GPSDATA3.gpx -x discard,fixnone -x interpolate,distance=0.001k -x track,faketime=f20120201120001+60 -o gpx -F tmp3.gpx
gpsbabel -D 3 -i gpx -f loops/GPSDATA4.gpx -x discard,fixnone -x interpolate,distance=0.001k -x track,faketime=f20130201120001+60 -o gpx -F tmp4.gpx
#
# combine together
gpsbabel -D 3 -i gpx -f tmp1.gpx -i gpx -f tmp2.gpx -i gpx -f tmp3.gpx -i gpx -f tmp4.gpx -x track,pack,title="Period" -o gpx -F tmp5.gpx
#
# resample timestamps
gpsbabel -D 3 -i gpx -f tmp5.gpx -x track,faketime=f20100201120001+10 -o gpx -F period.gpx
#
# prune points 
gpsbabel -D 3 -i gpx -f period.gpx -x position,distance=2m,time=120 -o gpx -F period_pruned.gpx
#
#
gpsbabel -D 3 -i gpx -f period_pruned.gpx -o xcsv,style=seninp.style -F period_pruned.csv
