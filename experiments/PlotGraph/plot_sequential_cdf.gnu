in1 = "res_sequential_strawman_8000_addSoft"
in2 = "res_sequential_inc_8000_addSoft"
in3 = "res_sequential_incacl_8000_addSoft"

set terminal pdf dashed font "Helvetica, 20" size 5,4 # monochrome
set datafile separator '\t'
set border lw 3

set xlabel 'Size of L3 Router Policy (# of Rules)'
set logscale x
set xtics nomirror

set ylabel 'CDF' offset 2
set yrange [0:1]
set ytics 0,0.2,1
set ytics nomirror

count = system("wc ".in1." | awk '{print $1}'")

# plot compilation time
set output 'Eval_sequential_compile_cdf.pdf'

set key right bottom

set xrange [0.01:10000]
set xtics (0.01,1,100,10000)
set xlabel 'Time (ms)'

plot    in1 u 1:(1./count) t 'Strawman' smooth cumulative lt 1 lc 1 lw 10,\
    in2 u 1:(1./count) t 'Incremental' smooth cumulative lt 2 lc 2 lw 10,\
    in3 u 1:(1./count) t 'IncreOpt' smooth cumulative lt 3 lc 3 lw 10

# plot rule-update
set output 'Eval_sequential_rule_cdf.pdf'

set key right bottom

set xrange [1:1000000]
set xtics (1,100,10000,'1000000' 1000000)
set xlabel '# of Flowmods'

plot    in1 u 2:(1./count) t 'Strawman' smooth cumulative lt 1 lc 1 lw 10,\
    in2 u 2:(1./count) t 'Incremental' smooth cumulative lt 2 lc 2 lw 10,\
    in3 u 2:(1./count) t 'IncreOpt' smooth cumulative lt 3 lc 3 lw 10

# plot total time hardware
set output 'Eval_sequential_total_hardware_cdf.pdf'

set key right bottom

set xrange [0.001:10000]
set xtics (0.001,0.1,10,1000)
set xlabel 'Time (s)'

plot    in1 u 5:(1./count) t 'Strawman' smooth cumulative lt 1 lc 1 lw 10,\
    in2 u 5:(1./count) t 'Incremental' smooth cumulative lt 2 lc 2 lw 10,\
    in3 u 5:(1./count) t 'IncreOpt' smooth cumulative lt 3 lc 3 lw 10

# plot total time software
set output 'Eval_sequential_total_software_cdf.pdf'

set key right bottom

set xrange [0.0001:1000]
set xtics (0.0001,0.01,1,100)
set xlabel 'Time (s)'

plot    in1 u 6:(1./count) t 'Strawman' smooth cumulative lt 1 lc 1 lw 10,\
    in2 u 6:(1./count) t 'Incremental' smooth cumulative lt 2 lc 2 lw 10,\
    in3 u 6:(1./count) t 'IncreOpt' smooth cumulative lt 3 lc 3 lw 10

