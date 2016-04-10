#!/bin/bash

sed 's/time=\(.*\)ms\(.*\)data\(.*\)corrid\(.*\)/data\3/g' $1
