#!/bin/bash

for f in p{dan,eve,galen,kieren,net,sar,zan}*.png; do convert $f -fill red -opaque '#9f7824' $f; done
for f in p{dan,eve,galen,kieren,net,sar,zan}*.png; do convert $f -fill red -opaque '#ff703d' $f; done
for f in p{dan,eve,galen,kieren,net,sar,zan}*.png; do convert $f -fill red -opaque '#4eceff' $f; done
for f in p{dan,eve,galen,kieren,net,sar,zan}*.png; do convert $f -fill red -opaque '#dedede' $f; done
for f in p{dan,eve,galen,kieren,net,sar,zan}*.png; do convert $f -fill red -opaque '#ffff3d' $f; done
for f in p{dan,eve,galen,kieren,net,sar,zan}*.png; do convert $f -fill red -opaque '#00a732' $f; done
for f in p{dan,eve,galen,kieren,net,sar,zan}*.png; do convert $f -fill red -opaque '#ff4efd' $f; done
for f in p{dan,eve,galen,kieren,net,sar,zan}*.png; do convert $f -fill '#655b53' -opaque '#0000ff' $f; done

