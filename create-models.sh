rm things/*

echo "running lein"
lein run src/dactyl_keyboard/dactyl.clj

for f in right bottom-plate
do
  echo "generating $f.stl"
  openscad -o "things/$f.stl" "things/$f.scad"
done
