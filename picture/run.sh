#!/bin/bash
# Level7代替案 - より確実な結果抽出（単一最良結果選択版）

# ディレクトリの作成
mkdir -p imgproc result

for image in $1/test/*.ppm; do
    bname=`basename ${image}`
    echo "Processing image: $bname"
    
    case $1 in
        "level7")
            # 最終結果ファイルの準備
            final_result="result/"`basename ${image} .ppm`".txt"
            > "${final_result}"
            
            # 全テンプレートの全結果を収集する一時ファイル
            all_results="temp_all_results.txt"
            > "$all_results"
            
            # 各テンプレートに対して個別に処理
            for template in $1/*.ppm; do
                original_template=`basename ${template}`
                echo "Processing template: $original_template"
                
                # このテンプレートの全結果を収集する一時ファイル
                template_results="temp_${original_template}.txt"
                > "$template_results"
                
                # Level 1 - 元画像そのまま
                echo "  Level 1 processing..."
                level1_name="imgproc/level1_"`basename ${image}`
                cp "${image}" "${level1_name}"
                tempname="imgproc/level1_$original_template"
                cp "${template}" "${tempname}"
                result=$(./matching "$level1_name" "$tempname" 0 1.5 p 2>/dev/null | grep "^\[Found")
                if [ -n "$result" ]; then
                    echo "    Found: $result"
                    echo "$result" >> "$template_results"
                fi
                
                # Level 2 - ブラー処理
                echo "  Level 2 processing..."
                level2_name="imgproc/level2_"`basename ${image}`
                convert -blur 2x6 "${image}" "${level2_name}"
                tempname="imgproc/level2_$original_template"
                cp "${template}" "${tempname}"
                result=$(./matching "$level2_name" "$tempname" 0 1.5 p 2>/dev/null | grep "^\[Found")
                if [ -n "$result" ]; then
                    echo "    Found: $result"
                    echo "$result" >> "$template_results"
                fi
                
                # Level 3 - 元画像そのまま（Level1と同じ）
                echo "  Level 3 processing..."
                level3_name="imgproc/level3_"`basename ${image}`
                cp "${image}" "${level3_name}"
                tempname="imgproc/level3_$original_template"
                cp "${template}" "${tempname}"
                result=$(./matching "$level3_name" "$tempname" 0 1.5 p 2>/dev/null | grep "^\[Found")
                if [ -n "$result" ]; then
                    echo "    Found: $result"
                    echo "$result" >> "$template_results"
                fi
                
                # Level 4 - 元画像そのまま（透明処理用）
                echo "  Level 4 processing..."
                level4_name="imgproc/level4_"`basename ${image}`
                cp "${image}" "${level4_name}"
                tempname="imgproc/level4_$original_template"
                cp "${template}" "${tempname}"
                result=$(./matching "$level4_name" "$tempname" 0 1.5 p 2>/dev/null | grep "^\[Found")
                if [ -n "$result" ]; then
                    echo "    Found: $result"
                    echo "$result" >> "$template_results"
                fi
                
                # Level 5 - ヒストグラム均等化 + スケール変更
                echo "  Level 5 processing..."
                level5_name="imgproc/level5_"`basename ${image}`
                convert -equalize "${image}" "${level5_name}"
                for size in 50 100 200; do
                    tempname="imgproc/level5_${size}_$original_template"
                    convert -resize "$size"% "${template}" "${tempname}"
                    result=$(./matching "$level5_name" "$tempname" 0 1.0 p 2>/dev/null | grep "^\[Found")
                    if [ -n "$result" ]; then
                        echo "    Found (${size}%): $result"
                        echo "$result" >> "$template_results"
                    fi
                done
                
                # Level 6 - 回転処理
                echo "  Level 6 processing..."
                level6_name="imgproc/level6_"`basename ${image}`
                cp "${image}" "${level6_name}"
                for rot in 0 90 180 270; do
                    tempname="imgproc/level6_${rot}_$original_template"
                    if [ $rot = 0 ]; then
                        cp "${template}" "${tempname}"
                    else
                        convert -rotate $rot "${template}" "${tempname}"
                    fi
                    result=$(./matching "$level6_name" "$tempname" $rot 1.5 p 2>/dev/null | grep "^\[Found")
                    if [ -n "$result" ]; then
                        echo "    Found (${rot}°): $result"
                        echo "$result" >> "$template_results"
                    fi
                done
                
                # このテンプレートの最良結果を選択して全体結果に追加
                if [ -s "$template_results" ]; then
                    echo "  All results for $original_template:"
                    cat "$template_results" | sed 's/^/    /'
                    
                    # 最小誤差を選択（7列目が距離値）
                    best_result=$(sort -k8 -n "$template_results" | head -1)
                    echo "  Best result for $original_template: $best_result"
                    echo "$best_result" >> "$all_results"
                else
                    echo "  No results found for $original_template"
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
                final_best_result=$(sort -k8 -n "$all_results" | head -1)
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
            echo "="
            ;;
    esac
done