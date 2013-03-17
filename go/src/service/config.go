package service

import (
	"appengine"
	//"appengine/memcache"
	"event"
	//"bytes"
	//"codec"
	//"strconv"
	"strings"
	"util"
)

type ServerConfig struct {
	RangeFetchLimit uint32
	CompressType    uint32
	EncryptType     uint32
	IsMaster        uint32
	RetryFetchCount uint32
	CompressFilter  map[string]string
	AllUsers        map[string]string
	Blacklist       map[string]string
}

func initServerConfig() *ServerConfig {
	cfg := new(ServerConfig)
	cfg.RetryFetchCount = 2
	cfg.RangeFetchLimit = 256 * 1024
	cfg.CompressType = event.COMPRESSOR_SNAPPY
	cfg.EncryptType = event.ENCRYPTER_SE1
	cfg.IsMaster = 0
	cfg.CompressFilter = make(map[string]string)
	cfg.AllUsers = make(map[string]string)
	cfg.Blacklist = make(map[string]string)
	return cfg
}

var Cfg = initServerConfig()

func fromIni(ini *util.Ini) {
	if tmp, exist := ini.GetIntProperty("Misc", "RangeFetchLimit"); exist {
		Cfg.RangeFetchLimit = uint32(tmp)
	}
	if tmp, exist := ini.GetIntProperty("Misc", "IsMaster"); exist {
		Cfg.IsMaster = uint32(tmp)
	}
	allusers, _ := ini.GetProperty("Auth", "Users")
	if len(allusers) > 0 {
		us := strings.Split(allusers, "|")
		for _, up := range us {
			tmp := strings.Split(up, ":")
			if len(tmp) == 2 {
				Cfg.AllUsers[tmp[0]] = tmp[1]
			}
		}
	}
	bs, _ := ini.GetProperty("Auth", "Blacklist")
	if len(bs) > 0 {
		bss := strings.Split(bs, "|")
		for _, b := range bss {
			Cfg.Blacklist[b] = "1"
		}
	}
	cs, _ := ini.GetProperty("Compress", "Filter")
	if len(cs) > 0 {
		css := strings.Split(cs, "|")
		for _, c := range css {
			Cfg.CompressFilter[c] = "1"
		}
	}
	ct, _ := ini.GetProperty("Compress", "Compressor")
	if strings.EqualFold(ct, "None") {
		Cfg.CompressType = event.COMPRESSOR_NONE
	}
}

func isValidUser(user, passwd string) bool {
	for u, p := range Cfg.AllUsers {
		if u == user && p == passwd {
			return true
		}
	}
	return false
}

func isContentTypeInCompressFilter(t string) bool {
	for k, _ := range Cfg.CompressFilter {
		if strings.Contains(t, k) {
			return true
		}
	}
	return true
}

func isInBlacklist(host string) bool {
	for k, _ := range Cfg.Blacklist {
		if strings.Contains(host, k) {
			return true
		}
	}
	return false
}

func LoadServerConfig(ctx appengine.Context) {
	ini, err := util.LoadIniFile("snova.conf")
	if nil != err {
		ctx.Errorf("Failed to load config:%v", err)
		return
	} else {
		ctx.Infof("Load config snova.conf success.")
	}
	fromIni(ini)
	//ctx.Infof("######%v  %d", Cfg.AllUsers, len(Cfg.AllUsers))
}
