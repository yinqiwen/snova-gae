package service

import (
	"appengine"
	"bytes"
	"event"
	"math/rand"
	"time"
	//"fmt"
)

const SEED = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz~!@#$%^&*()-+<>"

func generateRandomString(n int) string {
	s := ""
	src := rand.NewSource(time.Now().UnixNano())
	rnd := rand.New(src)
	for i := 0; i < n; i++ {
		index := rnd.Intn(len(SEED))
		s += SEED[index : index+1]
	}
	return s
}

func handleRecvEvent(tags *event.EventHeaderTags, ev event.Event, ctx appengine.Context) (event.Event, error) {
	var res event.Event
	switch ev.GetType() {
	case event.HTTP_REQUEST_EVENT_TYPE:
		res = Fetch(ctx, ev.(*event.HTTPRequestEvent))
	case event.AUTH_REQUEST_EVENT_TYPE:
		auth := ev.(*event.AuthRequestEvent)
		ares := new(event.AuthResponseEvent)
		if isValidUser(auth.User, auth.Passwd) {
			ares.Token = generateRandomString(8)
		} else {
			ares.Error = "Invalid User/Passwd"
		}
		res = ares
		//res = Auth(ctx, ev.(*event.AuthRequestEvent))
	case event.SHARE_APPID_EVENT_TYPE:
		res = HandleShareEvent(ctx, ev.(*event.ShareAppIDEvent))
	case event.REQUEST_SHARED_APPID_EVENT_TYPE:
		res = RetrieveAppIds(ctx)
	case event.EVENT_TCP_CHUNK_TYPE:
		res = TunnelWrite(ctx, tags, ev.(*event.TCPChunkEvent))
	case event.EVENT_TCP_CONNECTION_TYPE:
		res = TunnelSocketConnection(ctx, tags, ev.(*event.SocketConnectionEvent))
	case event.EVENT_SOCKET_CONNECT_WITH_DATA_TYPE:
		res = TunnelConn(ctx, tags, ev.(*event.SocketConnectWithDataEvent))
	case event.EVENT_SOCKET_READ_TYPE:
		res = TunnelRead(ctx, tags, ev.(*event.SocketReadEvent))
	}
	return res, nil
}

func HandleEvent(tags *event.EventHeaderTags, ev event.Event, ctx appengine.Context, sender EventSendService) error {
	res, err := handleRecvEvent(tags, ev, ctx)
	if nil != err {
		ctx.Errorf("Failed to handle event[%d:%d] for reason:%v", ev.GetType(), ev.GetVersion(), err)
		return err
	}
	if nil == res {
		var empty bytes.Buffer
		sender.Send(&empty)
		return nil
	}
	res.SetHash(ev.GetHash())
	compressType := Cfg.CompressType
	if httpres, ok := res.(*event.HTTPResponseEvent); ok {
		v := httpres.GetHeader("Content-Type")
		if len(v) > 0 && Cfg.CompressType != event.COMPRESSOR_NONE {
			if isContentTypeInCompressFilter(v) {
				compressType = event.COMPRESSOR_NONE
			}
		}
	}
	x := new(event.CompressEvent)
	x.SetHash(ev.GetHash())
	x.CompressType = compressType
	x.Ev = res
	y := new(event.EncryptEvent)
	y.SetHash(ev.GetHash())
	y.EncryptType = Cfg.EncryptType
	y.Ev = x
	var buf bytes.Buffer
	tags.Encode(&buf)
	event.EncodeEvent(&buf, y)
	sender.Send(&buf)
	return nil
}
