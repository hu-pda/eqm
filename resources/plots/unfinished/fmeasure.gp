resultfile = sprintf("%s%s", basepath, "/results/results.dat")
outputfile = sprintf("%s%s", basepath, "/plots/fmeasure.eps")
set title "F-measure curve" # chart title
set xlabel "Threshold" # x-axis label
set ylabel "F-measure" # y-axis label
set term postscript eps enhanced color 
set output outputfile # name of image file (change to .eps for eps file)
set xtics 0.1 # set increment for x-axis
set mxtics 1 # maximum value for x-axis
set style data lp # set default way of data plotting
set pointsize 1.5 # increase size of points on the line
set size square # set a square plot area
# [0:1] refers to min and max x-axis value, [0:0.9] for y-axis
# 1:2 refers to using column 1 of F-measure.data as x-axis, using column 2 as y-axis
# title is the name of experiment
# backslash indicates a concatenation operator
plot [0:1] [0:0.9] resultfile using 1:2 title "experiment 1", \
resultfile using 1:3 title "experiment 2", \
resultfile using 1:4 title "experiment 3"
reset # do not use any previous settings