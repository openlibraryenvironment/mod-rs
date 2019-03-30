

In this directory

run_external_reg.sh       -- Run this from the root of the grails project to start the executable jar as a service to okapi
register_and_enable.sh    -- Run this from the root of the grails project or the okapi_scripts directory to register the mod-rs module for the diku tenant

load_test_data.sh         -- Load some test data by talking to mod-rs via okapi
non_okapi_test.sh         -- Some example CURL commands for talking directly to the mod-rs service bypassing okapi

handy.sql                 -- Some handy postgres commands and queries you can use to interrogate the database after enabling mod-rs for diku and loading test data

okapi-cmd                 -- utility script to execute an okapi command
okapi-login               -- Utility script to log on to diku tenant as admin and spit out the JWT token for use in subsequent calls

README	                  -- This file




Using send161

./send161.groovy -s ReShare:DIKUA -r PALCI:ACMAIN -t "The Heart of Enterprise"
