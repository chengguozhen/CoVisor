in = 'res_gateway_all'

set terminal pdf dashed font "Helvetica, 20" size 5,4 # monochrome
set datafile separator '\t'
set border lw 3

#set xlabel 'IP Router Policy Size (# of Rules)' offset -2
set xtics nomirror

set style data histogram
set style histogram errorbars lw 3
set style fill solid border -1

set tmargin at screen 0.71
set bmargin at screen 0.12
set key outside c t
set key width -10

# plot compile time
set output 'Eval_gateway_compile.pdf'

set ylabel 'Time (ms)' offset 2
set logscale y
set yrange [1:100000]
set ytics (1,10,100,1000,10000,100000)
set ytics nomirror

plot in u 3:2:4:xtic(1) title 'Strawman [10, 50, 90 perc.]' lw 3 lt 1 lc rgb 'black' fs pattern 4,\
    in u 18:17:19:xtic(1) title 'Incremental [10, 50, 90 perc.]' lw 3 lt 1 lc rgb 'blue' fs pattern 2,\
    in u 33:32:34:xtic(1) title 'IncreOpt [10, 50, 90 perc.]' lw 3 lt 1 lc rgb 'red' fs pattern 1

# plot rule-update
set output 'Eval_gateway_rule.pdf'

set ylabel '# of Flowmods' offset 0
set logscale y
set yrange [100:10000000]
set ytics ('1e2' 100, '1e3' 1000, '1e4' 10000, '1e5' 100000, '1e6' 1000000, '1e7' 10000000)
set ytics nomirror

plot in u 6:5:7:xtic(1) title 'Strawman [10, 50, 90 perc.]' lw 3 lt 1 lc rgb 'black' fs pattern 4,\
    in u 21:20:22:xtic(1) title 'Incremental [10, 50, 90 perc.]' lw 3 lt 1 lc rgb 'blue' fs pattern 2,\
    in u 36:35:37:xtic(1) title 'IncreOpt [10, 50, 90 perc.]' lw 3 lt 1 lc rgb 'red' fs pattern 1

# plot total time hardware
set output 'Eval_gateway_total_hardware.pdf'

set ylabel 'Time (s)' offset 2
set logscale y
set yrange [1:100000]
set ytics (1,10,100,1000,10000,100000)
set ytics nomirror

plot in u 12:11:13:xtic(1) title 'Strawman [10, 50, 90 perc.]' lw 3 lt 1 lc rgb 'black' fs pattern 4,\
    in u 27:26:28:xtic(1) title 'Incremental [10, 50, 90 perc.]' lw 3 lt 1 lc rgb 'blue' fs pattern 2,\
    in u 42:41:43:xtic(1) title 'IncreOpt [10, 50, 90 perc.]' lw 3 lt 1 lc rgb 'red' fs pattern 1

# plot total time software
set output 'Eval_gateway_total_software.pdf'

set ylabel 'Time (s)' offset 2
set logscale y
set yrange [0.1:10000]
set ytics (0.1,1,10,100,1000,10000)
set ytics nomirror

plot in u 15:14:16:xtic(1) title 'Strawman [10, 50, 90 perc.]' lw 3 lt 1 lc rgb 'black' fs pattern 4,\
    in u 30:29:31:xtic(1) title 'Incremental [10, 50, 90 perc.]' lw 3 lt 1 lc rgb 'blue' fs pattern 2,\
    in u 45:44:46:xtic(1) title 'IncreOpt [10, 50, 90 perc.]' lw 3 lt 1 lc rgb 'red' fs pattern 1

