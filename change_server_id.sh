#!/bin/bash
sed -i 's/.*Server_token.*/Server_token='"S$1"'/' /home/roi/probecules/lycus/config.properties

