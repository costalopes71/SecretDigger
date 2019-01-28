package com.costalopes.secretdigger.digger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class SecretDigger {

	public List<String> digAndSearchDirectory(String word, File directoryToSearchFrom) throws Exception {

		//
		// getting the byte representation for the input word or phrase
		//
		byte[] pattern = word.getBytes();

		//
		// creating a result list with the path of the files that contains the keyword
		//
		List<String> resultList = new ArrayList<>();
		
		//
		// avoiding directories, only reading the bytes from files
		//
		Predicate<Path> directory = path -> !path.toFile().isDirectory();
		
		//
		// iterating over all the partitions or the directory that was chosen by the user
		//
		System.out.println("\nSEARCHING [" + directoryToSearchFrom + "]...");
			
		try (Stream<Path> walk = walk(directoryToSearchFrom.toPath());) {

			walk.filter(directory).forEach(path -> {

				try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path.toFile()));) {
					
					byte[] data = new byte[4098];
					
					//
					// buffering the data to the byte array, if that is no more data to buffer -1 will be returned
					//
					int number = bis.read(data);
					while (number != -1) {
						
						//
						// if the result of indexOf method is different from -1 it means that the word/phrase was found
						//
						if (indexOf(data, pattern) != -1) {
							System.out.println("==================================================");
							System.out.println("KEYWORD FOUND AT: " + path.toFile().getAbsolutePath());
							resultList.add(path.toFile().getAbsolutePath());
							System.out.println("==================================================");
							
							//
							// leave this file because the word was already found, we can go to the next file without analysing all data
							//
							bis.close();
							break;
						}
						
						number = bis.read(data);
					}
				} catch (IOException e) {
					System.out.println("Unable to access file at [" + path.toFile().getAbsolutePath()
							+ "], other process might have" + " blocked part of the file.");
				}

			});

		} catch (Exception e) {
			throw new IOException("error not expected [" + e.getMessage() + "]", e);
		}

		return resultList;
	}
	
	public List<String> digAndSearchEverywhere(String word) throws IOException {

		//
		// getting the byte representation for the input word or phrase
		//
		byte[] pattern = word.getBytes();

		//
		// creating a result file with the path of the files that contains the keyword
		//
		ArrayList<String> resultList = new ArrayList<>();
		
		//
		// avoiding directories, only reading the bytes from files
		//
		Predicate<Path> directory = path -> !path.toFile().isDirectory();
		
		//
		// iterating over all the partitions or the directory that was chosen by the user
		//
		for (File dir : File.listRoots()) {
			System.out.println("\nSEARCHING [" + dir + "]...");
			
			try (Stream<Path> walk = walk(dir.toPath());) {

				walk.filter(directory).forEach(path -> {

					try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path.toFile()));) {
						
						byte[] data = new byte[4098];
						
						//
						// buffering the data to the byte array, if that is no more data to buffer -1 will be returned
						//
						int number = bis.read(data);
						while (number != -1) {
							
							//
							// if the result of indexOf method is different from -1 it means that the word/phrase was found
							//
							if (indexOf(data, pattern) != -1) {
								System.out.println("==================================================");
								System.out.println("KEYWORD FOUND AT: " + path.toFile().getAbsolutePath());
								resultList.add(path.toFile().getAbsolutePath());
								System.out.println("==================================================");
								
								//
								// leave this file because the word was already found, we can go to the next file without analysing all data
								//
								break;
							}
							
							number = bis.read(data);
						}
					} catch (IOException e) {
						System.out.println("Unable to access file at [" + path.toFile().getAbsolutePath()
								+ "], other processar might have" + " blocked part of the file.");
					}

				});

			} catch (Exception e) {
				throw new IOException(e);
			}

		}
		
		return resultList;
	}

	public static Stream<Path> walk(Path p) {
		Stream<Path> s = Stream.of(p);

		if (Files.isDirectory(p))
			try {
				DirectoryStream<Path> ds = Files.newDirectoryStream(p);
				s = Stream.concat(s,
						StreamSupport.stream(ds.spliterator(), false).flatMap(SecretDigger::walk).onClose(() -> {
							try {
								ds.close();
							} catch (IOException ex) {
							}
						}));
			} catch (IOException ex) {
			}

		return s;
	}

	// in case you don’t want to ignore exceprions silently
	public static Stream<Path> walk(Path p, BiConsumer<Path, IOException> handler) {
		Stream<Path> s = Stream.of(p);
		if (Files.isDirectory(p))
			try {
				DirectoryStream<Path> ds = Files.newDirectoryStream(p);
				s = Stream.concat(s,
						StreamSupport.stream(ds.spliterator(), false).flatMap(sub -> walk(sub, handler)).onClose(() -> {
							try {
								ds.close();
							} catch (IOException ex) {
								handler.accept(p, ex);
							}
						}));
			} catch (IOException ex) {
				handler.accept(p, ex);
			}
		return s;
	}

	/**
	 * Finds the first occurrence of the pattern in the text.
	 */
	private static int indexOf(byte[] data, byte[] pattern) {
		if (data.length == 0)
			return -1;

		int[] failure = computeFailure(pattern);
		int j = 0;

		for (int i = 0; i < data.length; i++) {
			while (j > 0 && pattern[j] != data[i]) {
				j = failure[j - 1];
			}
			if (pattern[j] == data[i]) {
				j++;
			}
			if (j == pattern.length) {
				return i - pattern.length + 1;
			}
		}
		return -1;
	}

	/**
	 * Computes the failure function using a boot-strapping process, where the
	 * pattern is matched against itself.
	 */
	private static int[] computeFailure(byte[] pattern) {
		int[] failure = new int[pattern.length];

		int j = 0;
		for (int i = 1; i < pattern.length; i++) {
			while (j > 0 && pattern[j] != pattern[i]) {
				j = failure[j - 1];
			}
			if (pattern[j] == pattern[i]) {
				j++;
			}
			failure[i] = j;
		}

		return failure;
	}

}
