#!/bin/bash

sed 's/time=\(.*\)ms\(.*\)data\(.*\)/data\3ms\2/g' $1
