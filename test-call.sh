

# Test reading an entire vehicle
curl  --request GET --header "Content-Type:application/json" http://localhost:4404/api/vehicle

# Test reading a particular field from a vehicle
curl  --request GET --header "Content-Type:application/json" http://localhost:4404/api/vehicle.location

# Test reading from a vehicle by index
curl  --request GET --header "Content-Type:application/json" http://localhost:4404/api/vehicles.0.location

curl  --verbose --request POST --header "Content-Type:application/json" --data-binary @test-call.json http://localhost:4404/api/vehicle/gotoGuided
