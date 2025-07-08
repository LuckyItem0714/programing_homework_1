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
            convert -contrast "${image}" "${name}"
            ;;
        "level4")
            convert "${image}" "${name}"
            ;;
        "level5")
            convert -equalize "${image}" "${name}"
            ;;
        "level6")
            convert "${image}" "${name}"
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
    case $1 in
        "level1"|"level2"|"level3"|"level4")
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
            ;;
        "level5"|"level6")
            if [ $1 = "level5" ]
            then
                itre="50 100 200"
            else
                itre="0 90 180 270"
            fi
            for i in ${itre}; do
                echo "$bname: (scale/rot: $i)"
                for template in $1/*.ppm; do
                    if [ ! -f "$template" ]; then continue; fi
                    (
                        tempname="imgproc/"`basename ${template}`
                        if [ "$1" = "level5" ]; then
                            convert -resize "$i"% "${template}" "${tempname}"
                        else
                            if [ $i = 0 ]; then
                                convert "${template}" "${tempname}"
                            else
                                convert -rotate $i "${template}" "${tempname}"
                            fi
                        fi
                    ) &
                done
                wait
                for template in $1/*.ppm; do
                    if [ ! -f "$template" ]; then continue; fi
                    tempname="imgproc/"`basename ${template}`
                    if [ $1 = "level6" ]; then
                        rotation=$i
                    fi
                    
                    echo `basename ${template}`
                    if [ $x = 0 ]; then
                        ./matching $name "${tempname}" $rotation 1.5 cp
                        x=1
                    else
                        ./matching $name "${tempname}" $rotation 1.5 p
                    fi
                done
                echo ""
            done
            echo ""
            ;;
        *)
            echo $bname:
            for template in $1/*.ppm; do
                echo `basename ${template}`
	        if [ $x = 0 ]; then
	            ./matching $name "${template}" $rotation 1.5 cp 
	            x=1
	        else
	            ./matching $name "${template}" $rotation 1.5 p 
	        fi
            done
            echo ""
            ;;
    esac
done

for result_file in result/*.txt; do
    awk 'NR == 1 || $7 < min { min = $7; line = $0 } END { print line }' "$result_file" > tmp.txt
    mv tmp.txt "$result_file"
    echo "$result_file done"
done
wait
