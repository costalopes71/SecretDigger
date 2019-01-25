package com.costalopes.secretdigger.main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SecretDigger {
	
	private static final String RESULT_FILE = System.getProperty("java.io.tmpdir") + "secretDiggerResult.txt";
	
	public static void main(String[] args) {
		String word = printMenu();
		
		Instant start = Instant.now();
		
		try {
			digAndSearch(word);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("\nOoops...could not creat result file at [" + RESULT_FILE + "]");
			System.out.println("Aborting program...");
			System.exit(1);
		}
		
		System.out.println("\nFINISHED SEARCHING!");
		System.out.println("Result file can be found at [" + RESULT_FILE + "]");
		System.out.println("Elapsed: " + Duration.between(start, Instant.now()).toSeconds() + " seconds");
		
	}

	private static void digAndSearch(String word) throws FileNotFoundException {
		
		byte[] pattern = word.getBytes();
		System.out.println("\nSEARCHING ...");
		
		PrintWriter pw = new PrintWriter(new File(RESULT_FILE));

		//FIXME getting the storages and iterating over all the storages looking for the word 
		File aux = new File("C:/");
		
		
		Predicate<Path> directory = path -> !path.toFile().isDirectory();
		
		try (
				Stream<Path> walk = walk(aux.toPath());
			) {
			
				walk.filter(directory).forEach(path -> {
					
					try (
							BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path.toFile()));	
						){
						
						
						byte[] data = new byte[4098];
						int number = bis.read(data);
						while (number != -1) {
							
							if (indexOf(data, pattern) != -1) {
								System.out.println("==================================================");
								System.out.println("KEYWORD FOUND AT: " + path.toFile().getAbsolutePath());
								pw.println(path.toFile().getAbsolutePath());
								System.out.println("==================================================");
								bis.close();
								break;
							}
							number = bis.read(data);
						}
					} catch (IOException e) {
						System.out.println("Unable to access file at [" + path.toFile().getAbsolutePath() + "], other processar might have"
								+ " blocked part of the file.");
					}
						
				});
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		pw.close();
	}

	public static Stream<Path> walk(Path p) {
        Stream<Path> s= Stream.of(p);
        
        if(Files.isDirectory(p)) try {
            DirectoryStream<Path> ds = Files.newDirectoryStream(p);
            s=Stream.concat(s, StreamSupport.stream(ds.spliterator(), false)
                .flatMap(SecretDigger::walk)
                .onClose(()->{ try { ds.close(); } catch(IOException ex) {} }));
        } catch(IOException ex) {}
        
        return s;
    }
	
    // in case you don’t want to ignore exceprions silently
    public static Stream<Path> walk(Path p, BiConsumer<Path,IOException> handler) {
        Stream<Path> s=Stream.of(p);
        if(Files.isDirectory(p)) try {
            DirectoryStream<Path> ds = Files.newDirectoryStream(p);
            s=Stream.concat(s, StreamSupport.stream(ds.spliterator(), false)
                .flatMap(sub -> walk(sub, handler))
                .onClose(()->{ try { ds.close(); }
                               catch(IOException ex) { handler.accept(p, ex); } }));
        } catch(IOException ex) { handler.accept(p, ex); }
        return s;
    }
	
    /**
     * Finds the first occurrence of the pattern in the text.
     */
    public static int indexOf(byte[] data, byte[] pattern) {
        if (data.length == 0) return -1;

        int[] failure = computeFailure(pattern);    
        int j = 0;

        for (int i = 0; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) { j++; }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
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
    
    private static String printMenu() {
		System.out.println("================================================================================================");
		System.out.println("=================================== SECRET DIGGER ==============================================");
		System.out.println("================================================================================================");
		
		Scanner sc = new Scanner(System.in);
		System.out.print("Type the word or phrase that you want to search ->");
		String word = sc.next() + sc.nextLine();
		sc.close();
		return word;
	}
    
}
