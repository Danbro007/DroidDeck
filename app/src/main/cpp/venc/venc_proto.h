#ifndef DROIDDECK_VENC_PROTO_H
#define DROIDDECK_VENC_PROTO_H

#include <stdint.h>

/* SOCK_SEQPACKET between the guest's emulated /dev/video-enc0 and the app's MediaCodec encoder.
 * A config carries the buffer memfds (inputs first) as SCM_RIGHTS; every frame gets one reply. */

#define VENC_MAX_BUFFERS 16

enum venc_msg { VENC_CONFIG = 1, VENC_FRAME = 2, VENC_DONE = 3, VENC_BITRATE = 4 };
enum venc_codec { VENC_H264 = 0, VENC_HEVC = 1 };

struct venc_config {
    uint32_t type;
    uint32_t codec;
    uint32_t width, height, stride, rows;
    uint32_t fps_num, fps_den;
    uint32_t bitrate, peak_bitrate, vbr;
    uint32_t gop;
    int32_t profile, level;
    uint32_t inputs, outputs;
    uint32_t input_size, output_size;
};

struct venc_frame {
    uint32_t type;
    uint32_t input, output;
    uint32_t bytes;
    uint32_t keyframe;
    int64_t pts_us;
};

struct venc_done {
    uint32_t type;
    int32_t status;
    uint32_t bytes;
    uint32_t keyframe;
};

struct venc_bitrate {
    uint32_t type;
    uint32_t bitrate;
};

#endif
