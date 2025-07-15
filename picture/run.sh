#!/bin/sh
# Level7代替案 - より確実な結果抽出（単一最良結果選択版）
MAX_JOBS=5
for image in final_dataset/$1/*.ppm; do
    while [ $(jobs | wc -l) -ge $MAX_JOBS ]; do
        sleep 1
    done
    (
    bname=`basename ${image}`
    name="imgproc/"$bname
    x=0    	#
    echo $name

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
            convert "${image}" "${name}"
            ;;
        "level6")
            convert "${image}" "${name}"
            ;;
        "level7")
            # 最終結果ファイルの準備
            final_result="result/"`basename ${image} .ppm`".txt"
            mkdir -p result
            > "${final_result}"
            
            # 全テンプレートの全結果を収集する一時ファイル
            all_results="temp_all_results.txt"
            > "$all_results"
            
            # 各テンプレートに対して個別に処理
            for template in final_dataset/*.ppm; do
                original_template=`basename ${template}`
                echo "Processing template: $original_template"
                
                # このテンプレートの全結果を収集する一時ファイル
                template_results="temp_${original_template}.txt"
                
                # Level 1
                level1_name="imgproc/level1_"`basename ${image}`> "$template_results"
                convert "${image}" "${level1_name}"
                tempname="imgproc/level1_$original_template"
                convert "${template}" "${tempname}"
                # 修正: 結果からテンプレート名の部分だけを抽出
                result=$(./matching "$level1_name" "$tempname" 0 1.5 p 2>/dev/null | grep "^[a-zA-Z]" | sed "s|^[^/]*/||")
                if [ -n "$result" ]; then
                    echo "$result" >> "$template_results"
                fi
                
                # Level 2
                level2_name="imgproc/level2_"`basename ${image}`
                convert -blur 2x6 "${image}" "${level2_name}"
                tempname="imgproc/level2_$original_template"
                convert "${template}" "${tempname}"
                result=$(./matching "$level2_name" "$tempname" 0 1.5 p 2>/dev/null | grep "^[a-zA-Z]" | sed "s|^[^/]*/||")
                if [ -n "$result" ]; then
                    echo "$result" >> "$template_results"
                fi
                
                # Level 3
                level3_name="imgproc/level3_"`basename ${image}`
                convert "${image}" "${level3_name}"
                tempname="imgproc/level3_$original_template"
                convert "${template}" "${tempname}"
                result=$(./matching "$level3_name" "$tempname" 0 1.5 p 2>/dev/null | grep "^[a-zA-Z]" | sed "s|^[^/]*/||")
                if [ -n "$result" ]; then
                    echo "$result" >> "$template_results"
                fi
                
                # Level 4
                level4_name="imgproc/level4_"`basename ${image}`
                convert "${image}" "${level4_name}"
                tempname="imgproc/level4_$original_template"
                convert "${template}" "${tempname}"
                result=$(./matching "$level4_name" "$tempname" 0 1.5 p 2>/dev/null | grep "^[a-zA-Z]" | sed "s|^[^/]*/||")
                if [ -n "$result" ]; then
                    echo "$result" >> "$template_results"
                fi
                
                # Level 5
                level5_name="imgproc/level5_"`basename ${image}`
                convert -equalize "${image}" "${level5_name}"
                for size in 50 100 200; do
                    tempname="imgproc/level5_${size}_$original_template"
                    convert -resize "$size"% "${template}" "${tempname}"
                    result=$(./matching "$level5_name" "$tempname" 0 1.0 p 2>/dev/null | grep "^[a-zA-Z]" | sed "s|^[^/]*/||")
                    if [ -n "$result" ]; then
                        echo "$result" >> "$template_results"
                    fi
                done
                
                # Level 6
                level6_name="imgproc/level6_"`basename ${image}`
                convert "${image}" "${level6_name}"
                for rot in 0 90 180 270; do
                    tempname="imgproc/level6_${rot}_$original_template"
                    if [ $rot = 0 ]; then
                        convert "${template}" "${tempname}"
                    else
                        convert -rotate $rot "${template}" "${tempname}"
                    fi
                    result=$(./matching "$level6_name" "$tempname" $rot 1.5 p 2>/dev/null | grep "^[a-zA-Z]" | sed "s|^[^/]*/||")
                    if [ -n "$result" ]; then
                        echo "$result" >> "$template_results"
                    fi
                done
                
                # このテンプレートの最良結果を選択して全体結果に追加
                if [ -s "$template_results" ]; then
                    echo "All results for $original_template:"
                    cat "$template_results"
                    
                    # 最小誤差を選択（7列目が距離値）
                    best_result=$(sort -k7 -n "$template_results" | head -1)
                    echo "Best result for $original_template: $best_result"
                    echo "$best_result" >> "$all_results"
                else
                    echo "No results found for $original_template"
                fi
                
                # 一時ファイルを削除
                rm -f "$template_results"
                echo ""
            done
            
            # 全テンプレートの結果から最良の1つを選択
            if [ -s "$all_results" ]; then
                echo "All template results:"
                cat "$all_results"
                echo ""
                
                # 誤差率が最も小さい単一の結果を選択
                final_best_result=$(sort -k7 -n "$all_results" | head -1)
                echo "Final best result: $final_best_result"
                echo "$final_best_result" > "$final_result"
                
                echo "$bname final result:"
                cat "$final_result"
            else
                echo "No results found for any template"
                echo "" > "$final_result"
            fi
            
            # 一時ファイルを削除
            rm -f "$all_results"
            echo ""
            ;;
    esac
    
    rotation=0
    case $1 in
        "level1"|"level2"|"level3"|"level4")
            echo $bname:
            for template in final_dataset/*.ppm; do
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
                for template in final_dataset/*.ppm; do
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
                for template in final_dataset/*.ppm; do
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
            for template in final_dataset/*.ppm; do
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
    )&
done
wait
if [ $1 = "level5" ]; then
    for result_file in result/*.txt; do
        awk 'NR == 1 || $7 < min { min = $7; line = $0 } END { print line }' "$result_file" > tmp.txt
        mv tmp.txt "$result_file"
        echo "$result_file done"
    done
fi