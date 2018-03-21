resultfolder = sprintf("%s%s", basepath, "/results/perGroup/")
outputfile = sprintf("%s%s", basepath, "/plots/precision_recall_groups.eps")
colors="navy brown violet dark-green dark-gray"

FILES = system("ls -1 " . resultfolder . "*.dat")
LABEL = system("ls -1 " . resultfolder . "*.dat | sed -e 's:.*/::' -e 's/results-//' -e 's/.dat//'")
# The sed command replaces the resultfolder string and the ".dat"-String with emptystring so that we get nice labels

set term postscript eps enhanced color 

set title "Precision/Recall Comparison" # chart title
set xlabel "Precision" # x-axis label
set ylabel "Recall" # y-axis label
set xrange [0:1]
set yrange [0:1]
set key outside right

set output outputfile # name of image file (change to .eps for eps file)

# color definitions
set border linewidth 1.5
set style line 1 ps 1.5 lt 1 lw 2 # --- blue

# Axes
set style line 11 lc rgb '#808080' lt 1
set border 3 back ls 11
set tics nomirror out scale 0.75

# Grid
set style line 12 lc rgb'#808080' lt 0 lw 1
set grid back ls 12

# For each query plot one point corresponding to that line in the result file
#plot for[IDX=1:queries] resultfile using 5:6 every ::IDX::IDX w point linestyle 1 pt IDX lc rgb word(colors,IDX) title word(querynames,IDX) 

plot for [i=1:words(FILES)] word(FILES,i) u 5:6 title word(LABEL,i) noenhanced