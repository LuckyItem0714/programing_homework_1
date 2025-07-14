#include "imageUtil.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <math.h>

void templateMatchingGray(Image *src, Image *template, Point *position, double *distance)
{
	if (src->channel != 1 || template->channel != 1)
	{
		fprintf(stderr, "src and/or template image is not a gray image.\n");
		return;
	}

	double min_distance = INFINITY;
	int ret_x = 0;
	int ret_y = 0;
	int x, y, i, j;
	
	for (y = 0; y <= (src->height - template->height); y++)
	{
		for (x = 0; x <= (src->width - template->width); x++)
		{
			long long distance = 0;
			// SSD
			for (j = 0; j < template->height; j++)
			{
				for (i = 0; i < template->width; i++)
				{
					int v = (src->data[(y + j) * src->width + (x + i)] - template->data[j * template->width + i]);
					distance += (long long)v * v;
				}
			}
			double normalized_distance = sqrt((double)distance) / (template->width * template->height);
			if (normalized_distance < min_distance)
			{
				min_distance = normalized_distance;
				ret_x = x;
				ret_y = y;
			}
		}
	}

	position->x = ret_x;
	position->y = ret_y;
	*distance = min_distance;
}

void templateMatchingColor(Image *src, Image *template, Point *position, double *distance)
{
	if (src->channel != 3 || template->channel != 3)
	{
		fprintf(stderr, "src and/or template image is not a color image.\n");
		return;
	}

	double min_distance = INFINITY;
	int ret_x = 0;
	int ret_y = 0;
	int x, y, i, j;
	
	for (y = 0; y <= (src->height - template->height); y++)
	{
		for (x = 0; x <= (src->width - template->width); x++)
		{
			long long distance = 0;
			// SSD
			for (j = 0; j < template->height; j++)
			{
				for (i = 0; i < template->width; i++)
				{
					int pt = 3 * ((y + j) * src->width + (x + i));
					int pt2 = 3 * (j * template->width + i);
					int r = (src->data[pt + 0] - template->data[pt2 + 0]);
					int g = (src->data[pt + 1] - template->data[pt2 + 1]);
					int b = (src->data[pt + 2] - template->data[pt2 + 2]);

					distance += (long long)(r * r + g * g + b * b);
				}
			}
			double normalized_distance = sqrt((double)distance) / (template->width * template->height);
			if (normalized_distance < min_distance)
			{
				min_distance = normalized_distance;
				ret_x = x;
				ret_y = y;
			}
		}
	}

	position->x = ret_x;
	position->y = ret_y;
	*distance = min_distance;
}

void templateMatchingColorTransparent(Image *src, Image *template, Point *position, double *distance)
{
	if (src->channel != 3 || template->channel != 3)
	{
		fprintf(stderr, "src and/or template image is not a color image.\n");
		return;
	}

	double min_distance = INFINITY;
	int ret_x = 0;
	int ret_y = 0;
	int x, y, i, j;

	// 先にテンプレートの有効ピクセル数を計算
	int total_valid_pixels = 0;
	for (j = 0; j < template->height; j++)
	{
		for (i = 0; i < template->width; i++)
		{
			int pt2 = 3 * (j * template->width + i);
			int tr = template->data[pt2 + 0];
			int tg = template->data[pt2 + 1];
			int tb = template->data[pt2 + 2];

			if (tr > 10 || tg > 10 || tb > 10)
			{
				total_valid_pixels++;
			}
		}
	}

	if (total_valid_pixels == 0)
	{
		position->x = 0;
		position->y = 0;
		*distance = 0.0;
		return;
	}

	for (y = 0; y <= (src->height - template->height); y++)
	{
		for (x = 0; x <= (src->width - template->width); x++)
		{
			long long distance = 0;

			// SSD
			for (j = 0; j < template->height; j++)
			{
				for (i = 0; i < template->width; i++)
				{
					int pt = 3 * ((y + j) * src->width + (x + i));
					int pt2 = 3 * (j * template->width + i);

					// テンプレートのピクセルが黒かどうかをチェック
					int tr = template->data[pt2 + 0];
					int tg = template->data[pt2 + 1];
					int tb = template->data[pt2 + 2];

					// 黒ピクセルの判定（閾値は調整可能）
					if (tr <= 10 && tg <= 10 && tb <= 10)
					{
						// 黒ピクセルの場合はスキップ
						continue;
					}

					// 非黒ピクセルの場合は距離計算を行う
					int r = (src->data[pt + 0] - tr);
					int g = (src->data[pt + 1] - tg);
					int b = (src->data[pt + 2] - tb);
					distance += (long long)(r * r + g * g + b * b);
				}
			}

			double normalized_distance = sqrt((double)distance) / total_valid_pixels;
			if (normalized_distance < min_distance)
			{
				min_distance = normalized_distance;
				ret_x = x;
				ret_y = y;
			}
		}
	}

	position->x = ret_x;
	position->y = ret_y;
	*distance = min_distance;
}

void getRotatedPixel(int i, int j, int width, int height, int rotation, int *rot_i, int *rot_j)
{
	switch (rotation)
	{
	case 0: // 0度（回転なし）
		*rot_i = i;
		*rot_j = j;
		break;
	case 90: // 90度回転（時計回り）
		*rot_i = j;
		*rot_j = width - 1 - i;
		break;
	case 180: // 180度回転
		*rot_i = width - 1 - i;
		*rot_j = height - 1 - j;
		break;
	case 270: // 270度回転（反時計回り90度）
		*rot_i = height - 1 - j;
		*rot_j = i;
		break;
	}
}

// 回転したテンプレートのサイズを取得
void getRotatedSize(int width, int height, int rotation, int *new_width, int *new_height)
{
	if (rotation == 90 || rotation == 270)
	{
		*new_width = height;
		*new_height = width;
	}
	else
	{
		*new_width = width;
		*new_height = height;
	}
}

// 特定の角度での回転テンプレートマッチング
void templateMatchingColorWithRotation(Image *src, Image *template, Point *position, double *distance, int rotation)
{
	if (src->channel != 3 || template->channel != 3)
	{
		fprintf(stderr, "src and/or template image is not a color image.\n");
		return;
	}

	double min_distance = INFINITY;
	int best_x = 0, best_y = 0;
	int rot_width, rot_height;

	// 回転後のテンプレートサイズを取得
	getRotatedSize(template->width, template->height, rotation, &rot_width, &rot_height);

	// 元画像上でスライディングウィンドウ
	for (int y = 0; y <= (src->height - rot_height); y++)
	{
		for (int x = 0; x <= (src->width - rot_width); x++)
		{
			long long distance = 0;

			// SSD計算（回転したテンプレート）
			for (int j = 0; j < rot_height; j++)
			{
				for (int i = 0; i < rot_width; i++)
				{
					// 元画像のピクセル位置
					int src_pt = 3 * ((y + j) * src->width + (x + i));

					// 回転したテンプレートの対応するピクセル位置を計算
					int template_i, template_j;
					getRotatedPixel(i, j, rot_width, rot_height, rotation, &template_i, &template_j);

					// 元のテンプレート座標系でのピクセル位置
					int template_pt = 3 * (template_j * template->width + template_i);

					// RGB各チャンネルの差の二乗を計算
					int r = (src->data[src_pt + 0] - template->data[template_pt + 0]);
					int g = (src->data[src_pt + 1] - template->data[template_pt + 1]);
					int b = (src->data[src_pt + 2] - template->data[template_pt + 2]);
					distance += (long long)(r * r + g * g + b * b);
				}
			}

			// 最小距離の更新
			double normalized_distance = sqrt((double)distance) / (template->width * template->height);
			if (normalized_distance < min_distance)
			{
				min_distance = normalized_distance;
				best_x = x;
				best_y = y;
			}
		}
	}

	// 結果を返す
	position->x = best_x;
	position->y = best_y;
	*distance = min_distance;
}

int main(int argc, char **argv)
{
	if (argc < 5)
	{
		fprintf(stderr, "Usage: templateMatching src_image template_image rotation threshold option(c,w,p,g)\n");
		fprintf(stderr, "Option:\nc) clear a txt result. \nw) write result a image with rectangle.\np) print results.\n");
		fprintf(stderr, "ex: templateMatching src_image.ppm template_image.ppm 0 1.0  \n");
		fprintf(stderr, "ex: templateMatching src_image.ppm template_image.ppm 0 1.0 c\n");
		fprintf(stderr, "ex: templateMatching src_image.ppm template_image.ppm 0 1.0 w\n");
		fprintf(stderr, "ex: templateMatching src_image.ppm template_image.ppm 0 1.0 p\n");
		fprintf(stderr, "ex: templateMatching src_image.ppm template_image.ppm 0 1.0 g\n");
		fprintf(stderr, "ex: templateMatching src_image.ppm template_image.ppm 0 1.0 cw\n");
		fprintf(stderr, "ex: templateMatching src_image.ppm template_image.ppm 0 1.0 cwp\n");
		fprintf(stderr, "ex: templateMatching src_image.ppm template_image.ppm 0 1.0 cwpg\n");
		return -1;
	}

	char *input_file = argv[1];
	char *template_file = argv[2];
	int rotation = atoi(argv[3]);
	double threshold = atof(argv[4]);
	const char *level4 = "level4";

	char output_name_base[256];
	char output_name_txt[256];
	char output_name_img[256];
	strcpy(output_name_base, "result/");
	strcat(output_name_base, getBaseName(input_file));
	strcpy(output_name_txt, output_name_base);
	strcat(output_name_txt, ".txt");
	strcpy(output_name_img, output_name_base);

	int isWriteImageResult = 0;
	int isPrintResult = 0;
	int isGray = 0;

	if (argc == 6)
	{
		char *p = NULL;
		if ((p = strchr(argv[5], 'c')) != NULL)
			clearResult(output_name_txt);
		if ((p = strchr(argv[5], 'w')) != NULL)
			isWriteImageResult = 1;
		if ((p = strchr(argv[5], 'p')) != NULL)
			isPrintResult = 1;
		if ((p = strchr(argv[5], 'g')) != NULL)
			isGray = 1;
	}

	Image *img = readPXM(input_file);
	Image *template = readPXM(template_file);

	Point result;
	double distance = 0.0;

	if (isGray && img->channel == 3)
	{
		Image *img_gray = createImage(img->width, img->height, 1);
		Image *template_gray = createImage(template->width, template->height, 1);
		cvtColorGray(img, img_gray);
		cvtColorGray(template, template_gray);

		templateMatchingGray(img_gray, template_gray, &result, &distance);

		freeImage(img_gray);
		freeImage(template_gray);
	}
	else if (strstr(template_file, level4) != NULL)
	{
		templateMatchingColorTransparent(img, template, &result, &distance);
	}
	else if (rotation != 0)
	{
		templateMatchingColorWithRotation(img, template, &result, &distance, rotation);
	}
	else
	{
		templateMatchingColor(img, template, &result, &distance);
	}

	if (distance < threshold)
	{
		writeResult(output_name_txt, getBaseName(template_file), result, template->width, template->height, rotation, distance);
		if (isPrintResult)
		{
			printf("[Found] %s %d %d %d %d %d %f\n", getBaseName(template_file), result.x, result.y, template->width, template->height, rotation, distance);
		}
		if (isWriteImageResult)
		{
			drawRectangle(img, result, template->width, template->height);

			if (img->channel == 3)
				strcat(output_name_img, ".ppm");
			else if (img->channel == 1)
				strcat(output_name_img, ".pgm");
			printf("out: %s\n", output_name_img);
			writePXM(output_name_img, img);
		}
	}
	else
	{
		if (isPrintResult)
		{
			printf("[Not Found] %s %d %d %d %d %d %f (threshold: %f)\n", getBaseName(template_file), result.x, result.y, template->width, template->height, rotation, distance, threshold);
		}
	}

	freeImage(img);
	freeImage(template);
	return 0;
}