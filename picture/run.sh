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
            convert "${image}" "${name}"
            ;;
        "level7")
            # レベル7では各レベルに応じた画像処理を個別に適用
            # 一時結果ファイルを作成
            temp_result="temp_result_${bname}.txt"
            > "${temp_result}"  # ファイルを初期化
            
            # 基本画像（何もしない）を作成
            convert "${image}" "${name}"
            
            # Level 1: 何もしない（基本マッチング）
            echo "Testing Level 1 for $bname"
            level1_name="imgproc/level1_"`basename ${image}`
            convert "${image}" "${level1_name}"
            for template in $1/*.ppm; do
                tempname="imgproc/"`basename ${template}`
                convert "${template}" "${tempname}"
                ./matching $level1_name "${tempname}" 0 1.5 cp >> "${temp_result}"
            done
            
            # Level 2: ノイズ対応（入力画像にblur適用）
            echo "Testing Level 2 for $bname"
            level2_name="imgproc/level2_"`basename ${image}`
            convert -blur 2x6 "${image}" "${level2_name}"
            for template in $1/*.ppm; do
                tempname="imgproc/"`basename ${template}`
                convert "${template}" "${tempname}"
                ./matching $level2_name "${tempname}" 0 1.5 cp >> "${temp_result}"
            done
            
            # Level 3: コントラスト変化対応（基本画像使用）
            echo "Testing Level 3 for $bname"
            level3_name="imgproc/level3_"`basename ${image}`
            convert "${image}" "${level3_name}"
            for template in $1/*.ppm; do
                tempname="imgproc/"`basename ${template}`
                convert "${template}" "${tempname}"
                ./matching $level3_name "${tempname}" 0 1.5 cp >> "${temp_result}"
            done
            
            # Level 4: 透過テンプレート（基本画像使用）
            echo "Testing Level 4 for $bname"
            level4_name="imgproc/level4_"`basename ${image}`
            convert "${image}" "${level4_name}"
            for template in $1/*.ppm; do
                tempname="imgproc/"`basename ${template}`
                convert "${template}" "${tempname}"
                ./matching $level4_name "${tempname}" 0 1.5 cp >> "${temp_result}"
            done
            
            # Level 5: サイズ変更（equalize適用）
            echo "Testing Level 5 for $bname"
            level5_name="imgproc/level5_"`basename ${image}`
            convert -equalize "${image}" "${level5_name}"
            for size in 50 100 200; do
                for template in $1/*.ppm; do
                    tempname="imgproc/"`basename ${template}`
                    convert -resize "$size"% "${template}" "${tempname}"
                    ./matching $level5_name "${tempname}" 0 1.0 cp >> "${temp_result}"
                done
            done
            
            # Level 6: 回転（基本画像使用）
            echo "Testing Level 6 for $bname"
            level6_name="imgproc/level6_"`basename ${image}`
            convert "${image}" "${level6_name}"
            for rot in 0 90 180 270; do
                for template in $1/*.ppm; do
                    tempname="imgproc/"`basename ${template}`
                    if [ $rot = 0 ]
                    then
                        convert "${template}" "${tempname}"
                    else
                        convert -rotate $rot "${template}" "${tempname}"
                    fi
                    ./matching $level6_name "${tempname}" $rot 1.5 cp >> "${temp_result}"
                done
            done
            
            # 各テンプレートに対して最小誤差を持つ結果のみを抽出（閾値以下の結果のみから）
            final_result="result/"`basename ${image} .ppm`".txt"
            > "${final_result}"  # 最終結果ファイルを初期化
            
            # 一時結果ファイルから各テンプレートの最小誤差を見つける
            for template in $1/*.ppm; do
                template_basename=`basename ${template}`
                
                # 該当するテンプレートの結果のみを抽出し、誤差でソート（閾値以下の発見されたもののみ）
                grep "${template_basename}" "${temp_result}" | sort -k7 -n | head -1 >> "${final_result}"
            done
            
            # 一時ファイルを削除
            rm -f "${temp_result}"
            
            # 結果を表示
            echo "$bname results:"
            cat "${final_result}"
            echo ""
            ;;
        "level8")
            convert "${image}" "${name}"
            ;;
        *)
            echo "Unknown level: $1"
            convert "${image}" "${name}"  # デフォルトは何もしない
            ;;
    esac

    # level7以外の処理
    if [ "$1" != "level7" ]; then
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
                    echo $bname:
                    for template in $1/*.ppm; do
                        tempname="imgproc/"`basename ${template}`
                        if [ $1 = "level5" ]
                        then
                            convert -resize "$i"% "${template}" "${tempname}"
                        else
                            if [ $i = 0 ]
                            then
                                convert "${template}" "${tempname}"
                            else
                                convert -rotate $i "${template}" "${tempname}"
                            fi
                            rotation=$i
                        fi
                        echo `basename ${template}`
                        if [ $x = 0 ];
                        then
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
        esac
    fi
done
wait