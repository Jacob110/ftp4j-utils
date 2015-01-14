package com.infosky.yanjj;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPFile;

import it.sauronsoftware.ftp4j.FTPListParseException;
import it.sauronsoftware.ftp4j.FTPListParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FTP上传、下载工具类
 */
public class FtpUtils {

	/**
	 * FTP上传本地文件到FTP的一个目录下
	 * 
	 * @param serverIP
	 *            FTP 地址
	 * @param serverPort
	 *            FTP 端口
	 * @param ftpUser
	 *            登录用户名
	 * @param ftpPwd
	 *            密码
	 * @param localFilePath
	 *            本地文件路径
	 * @param remoteFilePath
	 *            FTP上传目录
	 * @param proxyHost
	 *            代理服务器地址
	 * @param proxyPort
	 *            代理服务器端口
	 * @throws Exception
	 */
	public static void upload(String serverIP, int serverPort, String ftpUser,
			String ftpPwd, String localFilePath, String remoteFilePath,
			String proxyHost, Integer proxyPort) throws Exception {
		// 定义 FTP Client
		FTPClient client = null;
		try {
			// 获得 FTP client
			client = getFtpConnection(serverIP, serverPort, ftpUser, ftpPwd);
			// 读取本地文件
			File localFile = new File(localFilePath);

			// 判断远程路径是否存在
			int isExists = isExist(client, remoteFilePath);
			// 不存在创建目录
			if (isExists < 0) {
				client.createDirectory(remoteFilePath);
			}
			// 切换到 目标路径
			client.changeDirectory(remoteFilePath);
			// 上传 文件
			// System.out.println(client.currentDirectory());
			String tempPath = client.currentDirectory();
			// 上传过程中 先生成 .tmp 临时文件
			InputStream inputStream = new FileInputStream(localFile);
			client.upload(localFile.getName() + ".tmp", inputStream, 0L, 0L,
					new CustomTransferListener("FTP Upload"));
			// client.upload(localFile);
			// 修改文件名
			FTPFile[] ftpFiles = client.list();
			for (FTPFile ff : ftpFiles) {
				if (ff.getName().equals(localFile.getName() + ".tmp")) {
					// 上传完成 修改文件名
					client.rename(ff.getName(), localFile.getName());
				}
			}
			// 上传完成 切回 根目录
			client.changeDirectory("/");
		} finally {
			if (client != null) {
				if (client.isConnected()) {
					client.logout();
					client.disconnect(true);
				}
			}
			client = null;
		}
	}

	public static void upload(String serverIP, int serverPort, String ftpUser,
			String ftpPwd, String localFilePath, String remoteFilePath)
			throws Exception {
		upload(serverIP, serverPort, ftpUser, ftpPwd, localFilePath,
				remoteFilePath, null, null);
	}

	/**
	 * FTP下载文件到本地一个文件夹,如果本地文件夹不存在，则创建必要的目录结构
	 * 
	 * @param serverIP
	 *            FTP 地址
	 * @param serverPort
	 *            FTP 端口
	 * @param ftpUser
	 *            登录用户名
	 * @param ftpPwd
	 *            密码
	 * @param localFilePath
	 *            本地文件路径
	 * @param remoteFilePath
	 *            FTP上传目录
	 * @param proxyHost
	 *            代理服务器地址
	 * @param proxyPort
	 *            代理服务器端口
	 * @throws Exception
	 */
	public static void download(String serverIP, int serverPort,
			String ftpUser, String ftpPwd, String localFilePath,
			String remoteFilePath, String proxyHost, Integer proxyPort)
			throws Exception {
		// 定义 FTP Client
		FTPClient client = null;
		try {
			// 获得 FTP client
			client = getFtpConnection(serverIP, serverPort, ftpUser, ftpPwd);
			// 检查本地保存路径
			File localFolder = new File(localFilePath);
			// 如果目标文件夹不存在 则创建
			if (!localFolder.isFile() && !localFolder.exists()) {
				localFolder.mkdirs();
			}
			// 判断远程路径是否存在
			int isExists = isExist(client, remoteFilePath);
			// 获取文件名
			String remoteFileName = new File(remoteFilePath).getName();
			// 下载操作
			if (isExists == FTPFile.TYPE_FILE) {
				// client.changeDirectory(remoteFilePath);
				System.out.println(client.currentDirectory());
				// / 先生成 临时文件
				File tmpFile = new File(localFolder + File.separator
						+ remoteFileName + ".tmp");
				client.download(remoteFilePath, tmpFile,
						new CustomTransferListener("FTP Download"));
				// 下载完成 改回文件名
				tmpFile.renameTo(new File(localFolder + File.separator
						+ remoteFileName));
			}

			// 上传完成 切回 根目录
			client.changeDirectory("/");
		} finally {
			if (client != null) {
				if (client.isConnected()) {
					client.logout();
					client.disconnect(true);
				}
			}
			client = null;
		}
	}

	public static void download(String serverIP, int serverPort,
			String ftpUser, String ftpPwd, String localFilePath,
			String remoteFilePath) throws Exception {
		download(serverIP, serverPort, ftpUser, ftpPwd, localFilePath,
				remoteFilePath, null, null);
	}

	/**
	 * FTP 删除文件或文件夹
	 * 
	 * @param serverIP
	 *            FTP 地址
	 * @param serverPort
	 *            FTP 端口
	 * @param ftpUser
	 *            登录用户名
	 * @param ftpPwd
	 *            密码
	 * @param remoteFilePath
	 *            FTP上传目录
	 * @param proxyHost
	 *            代理服务器地址
	 * @param proxyPort
	 *            代理服务器端口
	 * @throws Exception
	 */
	public static void delete(String serverIP, int serverPort, String ftpUser,
			String ftpPwd, String remoteFilePath, String proxyHost,
			Integer proxyPort) throws Exception {
		// 定义 FTP Client
		FTPClient client = null;
		try {
			// 获得 FTP client
			client = getFtpConnection(serverIP, serverPort, ftpUser, ftpPwd);

			// 判断远程路径是否存在
			int isExists = isExist(client, remoteFilePath);
			// 获取文件名
			String remoteFileName = new File(remoteFilePath).getName();
			// 删除文件
			if (isExists == FTPFile.TYPE_FILE) {
				client.deleteFile(remoteFilePath);
			}
			// 删除文件夹
			if (isExists == FTPFile.TYPE_DIRECTORY) {
				deleteFolder(client, remoteFilePath);
			}
			// 删除完成 切回 根目录
			client.changeDirectory("/");
		} finally {
			if (client != null) {
				if (client.isConnected()) {
					client.logout();
					client.disconnect(true);
				}
			}
			client = null;
		}
	}

	/**
	 * FTP 删除文件夹 私有方法
	 * 
	 * @param client
	 *            FTP Client
	 * @param filePath
	 *            文件夹路径
	 */
	private static void deleteFolder(FTPClient client, String filePath)
			throws Exception {
		String path = filePath;
		client.changeDirectory(path);
		FTPFile[] files = client.list();
		String name = null;

		for (FTPFile file : files) {
			name = file.getName();
			// 排除隐藏目录
			if (".".equals(name) || "..".equals(name)) {
				continue;
			}
			if (file.getType() == FTPFile.TYPE_DIRECTORY) { // 递归删除子目录
				deleteFolder(client, client.currentDirectory() + File.separator
						+ file.getName());
			} else if (file.getType() == FTPFile.TYPE_FILE) { // 删除文件
				client.deleteFile(file.getName());
			}
		}
		client.changeDirectoryUp();// 反回上一级目录
		client.deleteDirectory(filePath); // 删除当前目录
	}

	public static void delete(String serverIP, int serverPort, String ftpUser,
			String ftpPwd, String remoteFilePath) throws Exception {
		delete(serverIP, serverPort, ftpUser, ftpPwd, remoteFilePath, null,
				null);
	}

	/**
	 * 创建FTP连接
	 * 
	 * @param host
	 *            主机名或IP
	 * @param port
	 *            ftp端口
	 * @param username
	 *            ftp用户名
	 * @param password
	 *            ftp密码
	 * @return ftpClient 一个客户端
	 */
	private static FTPClient getFtpConnection(String host, int port,
			String username, String password) throws Exception {
		FTPClient client = new FTPClient();
		client.connect(host, port);
		client.login(username, password);
		client.setPassive(true);
		client.setType(FTPClient.TYPE_BINARY);
		return client;
	}

	/**
	 * 判断一个FTP路径是否存在，如果存在返回类型(FTPFile.TYPE_DIRECTORY=1、FTPFile.TYPE_FILE=0、
	 * FTPFile.TYPE_LINK=2) 如果文件不存在，则返回一个-1
	 * 
	 * @param client
	 *            FTP客户端
	 * @param remotePath
	 *            FTP文件或文件夹路径
	 * @return 存在时候返回类型值(文件0，文件夹1，连接2)，不存在则返回-1
	 */
	private static int isExist(FTPClient client, String remotePath) {
		int x = -1;
		FTPFile[] list = null;
		try {
			list = client.list(remotePath);
		} catch (Exception e) {
			return -1;
		}
		if (list.length > 1)
			return FTPFile.TYPE_DIRECTORY;
		else if (list.length == 1) {
			FTPFile f = list[0];
			if (f.getType() == FTPFile.TYPE_DIRECTORY)
				return FTPFile.TYPE_DIRECTORY;
			// 假设推理判断
			String _path = remotePath + "/" + f.getName();
			try {
				int y = client.list(_path).length;
				if (y == 1)
					return FTPFile.TYPE_DIRECTORY;
				else
					return FTPFile.TYPE_FILE;
			} catch (Exception e) {
				return FTPFile.TYPE_FILE;
			}
		} else {
			try {
				client.changeDirectory(remotePath);
				return FTPFile.TYPE_DIRECTORY;
			} catch (Exception e) {
				return -1;
			}
		}
	}

	// FTP 传输 监视器 ,简单实现
	private static class CustomTransferListener implements
			FTPDataTransferListener {
		private String optType;

		private boolean isCompleted = false;

		public CustomTransferListener() {
			super();
		}

		public CustomTransferListener(String optType) {
			this.optType = optType;
		}

		public void started() {
			System.out.println(optType + ": FTP Starting...");
		}

		public void transferred(int length) {
			System.out.println(optType + ":Transferring..." + length);
		}

		public void completed() {
			isCompleted = true;
			System.out.println(optType + ":Complete!");
		}

		public void aborted() {
			System.out.println(optType + ":Abort!");
		}

		public void failed() {
			System.out.println(optType + ":Failed!");
		}
	}

	/**
	 * 重写 DOSListParser
	 */
	private class CustomDOSListParser implements FTPListParser {

		private final Pattern PATTERN = Pattern
				.compile("^(\\d{2})-(\\d{2})-(\\d{2})\\s+(\\d{2}):(\\d{2})(AM|PM)\\s+"
						+ "(<DIR>|\\d+)\\s+([^\\\\/*?\"<>|]+)$");
		// 自定义 修改为 Locale.ENGLISH 解决 FTPClien.list() 获取不到 文件项的问题
		private final DateFormat DATE_FORMAT = new SimpleDateFormat(
				"MM/dd/yy hh:mm a", Locale.ENGLISH);

		public FTPFile[] parse(String[] lines) throws FTPListParseException {
			int size = lines.length;
			FTPFile[] ret = new FTPFile[size];
			for (int i = 0; i < size; i++) {
				Matcher m = PATTERN.matcher(lines[i]);
				if (m.matches()) {
					String month = m.group(1);
					String day = m.group(2);
					String year = m.group(3);
					String hour = m.group(4);
					String minute = m.group(5);
					String ampm = m.group(6);
					String dirOrSize = m.group(7);
					String name = m.group(8);
					ret[i] = new FTPFile();
					ret[i].setName(name);
					if (dirOrSize.equalsIgnoreCase("<DIR>")) {
						ret[i].setType(FTPFile.TYPE_DIRECTORY);
						ret[i].setSize(0);
					} else {
						long fileSize;
						try {
							fileSize = Long.parseLong(dirOrSize);
						} catch (Throwable t) {
							throw new FTPListParseException();
						}
						ret[i].setType(FTPFile.TYPE_FILE);
						ret[i].setSize(fileSize);
					}
					String mdString = month + "/" + day + "/" + year + " "
							+ hour + ":" + minute + " " + ampm;
					Date md;
					try {
						synchronized (DATE_FORMAT) {
							md = DATE_FORMAT.parse(mdString);
						}
					} catch (ParseException e) {
						throw new FTPListParseException();
					}
					ret[i].setModifiedDate(md);
				} else {
					throw new FTPListParseException();
				}
			}
			return ret;
		}
	}
}
