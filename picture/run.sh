#!/bin/sh
# imagemagickで何か画像処理をして，/imgprocにかきこみ，テンプレートマッチング
# 最終テストは，直下のforループを次に変更 for image in $1/final/*.ppm; do
for image in $1/test/*.ppm; do
    bname=`basename ${image}`
    name="imgproc/"$bname
    x=0    	#
    echo $name
    
    # レベルに応じて画像処理を変更
    case $1 in
        "level1")
            convert "${image}" "${name}"  # 何もしない画像処理
            ;;
        "level2")
            convert -blur 2x6 "${image}" "${name}"
            ;;
        "level3")
            convert "${image}" "${name}"
            ;;
        "level4")
            convert "${image}" "${name}"
            ;;
        "level5")
            convert -equalize "${image}" "${name}"
            ;;
        "level6")
            
            for angle in 0 90 180; do
                tempname = "imgproc/"'basename'
                rotation=0
                echo $bname:
                for template in $1/*.ppm; do
                echo `basename ${template}`
                if [ angle = 0 ]
                then
                    ./matching $name "${template}" $rotation 1.5 cp 
                    x=1
                else
                    ./matching $name "${template}" $rotation 1.5 p 
                fi
                done
                echo ""
            convert -rotate 270 "${image}" "${name}"
            done
            ;;
        "level7")
            convert -sharpen 0x1 "${image}" "${name}"
            ;;
        "level8")
            convert -normalize "${image}" "${name}"
            ;;
        *)
            echo "Unknown level: $1"
            convert "${image}" "${name}"  # デフォルトは何もしない
            ;;
    esac
    
    rotation=0
    echo $bname:
    for template in $1/*.ppm; do
	echo `basename ${template}`
	if [ $x = 0 ]
	then
	    ./matching $name "${template}" $rotation 1.5 cp 
	    x=1
	else
	    ./matching $name "${template}" $rotation 1.5 p 
	fi
    done
    echo ""
done
wait