# gpsbabel -i gpx -f tmp.gpx -x track,speed,course -o gpx -F part1.gpx
#
# fake time from part1 and interpolate by distance
gpsbabel -D 3 -i gpx -f loops/GPSDATA1.gpx -x track,faketime=f20140201120001+10 -x interpolate,distance=0.0003k -o gpx -F tmp1.gpx
gpsbabel -D 3 -i gpx -f loops/GPSDATA2.gpx -x track,faketime=f20140302120001+10 -x interpolate,distance=0.0003k -o gpx -F tmp2.gpx
gpsbabel -D 3 -i gpx -f loops/GPSDATA3.gpx -x track,faketime=f20140403120001+10 -x interpolate,distance=0.0003k -o gpx -F tmp3.gpx
gpsbabel -D 3 -i gpx -f loops/GPSDATA4.gpx -x track,faketime=f20140504120001+10 -x interpolate,distance=0.0003k -o gpx -F tmp4.gpx
#
# combine together
gpsbabel -D 3 -i gpx -f tmp1.gpx -i gpx -f tmp2.gpx -i gpx -f tmp3.gpx -i gpx -f tmp4.gpx -x track,merge,title="Period" -o gpx -F period.gpx
#     
# make a CSV out of GPX 
#
gpsbabel -D 3 -i gpx -f period.gpx -o xcsv,style=seninp.style -F period.csv
#     
# remove points which are within 5 meters 
#   
gpsbabel -D 3 -i gpx -f period.gpx -x transform,wpt=trk -x position,distance=1m -o gpx -F period_pruned.gpx
#
# convert this to CSV too
#   
gpsbabel -D 3 -i gpx -f period_pruned.gpx -o xcsv,style=seninp.style -F period_pruned.csv
#
#
cat period_hilbert_curve.csv | cut -d',' -f2 >period_hilbert_curve_4Sequitur.csv
