package com.infosky.yanjj.test;

import org.junit.Test;
import com.infosky.yanjj.FtpUtils;

public class FtpUtilTest {
	@Test
	public void deleteTest() throws Exception {
		// remote ftp server
		String server = "localhost";
		String user = "username";
		String pass = "password";
		int port = 21;
		String filePath = "D:/1.sql";
		String remoteFilePath = "/ftptest/1.sql";
		// upload
		FtpUtils.delete(server, port, user, pass, remoteFilePath);
	}

	@Test
	public void uploadTest() throws Exception {
		// remote ftp server
		String server = "localhost";
		String user = "username";
		String pass = "password";
		int port = 21;
		String filePath = "D:/ksb.zip";
		String remoteFilePath = "/ftptest/test1/";
		// upload
		FtpUtils.upload(server, port, user, pass, filePath, remoteFilePath);
	}

	@Test
	public void downloadTest() throws Exception {
		// remote ftp server
		String server = "localhost";
		String user = "username";
		String pass = "password";
		int port = 21;
		String filePath = "D:/ftpDownload/";
		String remoteFilePath = "/ftptest/test1/ksb.zip";
		// download
		FtpUtils.download(server, port, user, pass, filePath, remoteFilePath);
	}
}
