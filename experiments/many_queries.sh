sudo killall java
sleep 1
sudo python queries.py expr-par &
# Wait for CoVisor to start.
sleep 60
for a in `seq 100`
do
  echo -n "$a "
  #sudo python queries.py query &
   sudo python queries.py query1 "$a" &
done  
