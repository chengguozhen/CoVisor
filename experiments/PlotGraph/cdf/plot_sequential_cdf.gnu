in1 = "res_sequential_strawman_32000"
in2 = "res_sequential_inc_32000"
in3 = "res_sequential_incacl_32000"

set terminal pdf dashed font "Helvetica, 16" size 5,4 # monochrome
set datafile separator '\t'
set border lw 3


set grid

#set key box
set key right bottom

set output 'Eval.pdf'

set yrange [0:1]
set ytics 0,0.2,1
set ylabel 'CDF' offset 2

#set xrange [0:30]
#set xtics 0,5,30
set logscale x
set xlabel 'Time (second)'

count = system("wc ".in1." | awk '{print $1}'")

plot    in1 u 3:(3./count) t 'Control' smooth cumulative lt 1 lc 1 lw 10,\
    in2 u 3:(3./count) t 'Control' smooth cumulative lt 1 lc 1 lw 10,\
    in3 u 3:(3./count) t 'Control' smooth cumulative lt 1 lc 1 lw 10

