#!/bin/bash

sed 's/time=\(.*\)ms\(.*\)data\(.*\)ms\(.*\)corrid\(.*\)/data\3ms\2/g' $1
