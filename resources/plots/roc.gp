set title "ROC curve" # chart title
set xlabel "False positive rate" # x-axis label
set ylabel "True positive rate" # y-axis label
set term postscript eps enhanced color 
set output outputfile # name of image file (change to .eps for eps file)
set xtics 0.1 # set increment for x-axis
set mxtics 1 # maximum value for x-axis
set key right bottom # move legend to bottom right
set style data lp # set default way of data plotting
set pointsize 1.5 # increase size of points on the line
set size square # set a square plot area
# [0:1] refers to min and max x-axis value, [0:1] for y-axis
# 1:2 refers to using column 1 of ROC.data as x-axis, using column 2 as y-axis
# title is the name of experiment
# backslash indicates a concatenation operator
plot [0:1] [0:1] resultfile using 1:2 title "experiment 1", \
resultfile using 1:3 title "experiment 2", \
resultfile using 1:4 title "experiment 3", \
resultfile using 1:5 title "random" with lines
reset # do not use any previous settings