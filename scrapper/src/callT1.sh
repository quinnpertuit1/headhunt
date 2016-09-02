a=1001
b=1011

while [ $a -lt 2000 ]
do
   echo $a  "  "  $b
   Rscript LinkedinResumeParallel.R $a $b &
   if [ $a -eq 2000 ]
   then
      break
   fi
   a=`expr $a + 10`
   b=`expr $b + 10`
done
