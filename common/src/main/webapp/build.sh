echo building coffeescripts
coffee -o js/ coffee/
echo building checklist
haml checklist/plane.haml checklist/plane.html
haml checklist/copter.haml checklist/copter.html
echo building README
haml README.haml README.html

