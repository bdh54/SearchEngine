package prog11;

import prog05.ArrayQueue;

import java.util.*;

public class NotGPT implements SearchEngine {

    Disk pageDisk = new Disk();
    Disk wordDisk = new Disk();

    Map<String, Long> word2index = new HashMap<>();
    Map<String, Long> url2index = new TreeMap<>();

    @Override
    public void collect(Browser browser, List<String> startingURLs) {
        ArrayQueue<Long> pageIndices = new ArrayQueue<>();
        for (String url : startingURLs) {
            if (!url2index.containsKey(url)) {
                pageIndices.add(indexPage(url));
            }
        }
        while (pageIndices.size != 0) {
            List<String> urls;
            List<String> words;
            InfoFile file = pageDisk.get(pageIndices.poll());
            if (browser.loadPage(file.data)) {
                urls = browser.getURLs();
                Set<String> urlsForInfo = new HashSet<>();
                for (String url : urls) {
                    if (!url2index.containsKey(url)) {
                        long index = indexPage(url);
                        pageIndices.add(index);
                    }
                    if (urlsForInfo.add(url) == true) {
                        file.indices.add(url2index.get(url));
                    }
                }
                words = browser.getWords();
                Set<String> noDuplicateWords = new HashSet<>();
                for (String word : words) {
                    if (!word2index.containsKey(word)) {
                        indexWord(word);
                    }
                    if (noDuplicateWords.add(word) == true) {
                        wordDisk.get(word2index.get((word))).indices.add(url2index.get((file.data)));
                    }
                }

            }

        }

    }

    @Override
    public void rank(boolean fast) {
        for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()) {
            long index = entry.getKey();
            InfoFile file = entry.getValue();
            file.priority = 1.0;
            file.tempPriority = 0.0;
        }
        double count = 0;
        for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()) {
            InfoFile file = entry.getValue();
            if (file.indices.size() == 0) {
                ++count;
            }
        }
        double defaultPriority = 1 * count / pageDisk.size();
        if (!fast) {
            for (int i = 0; i < 20; ++i) {
                rankSlow(defaultPriority);
            }
        } else {
            for (int i = 0; i < 20; ++i) {
                rankFast(defaultPriority);
            }
        }
    }

    @Override
    public String[] search(List<String> searchWords, int numResults) {

        Iterator<Long>[] wordFileIterators =
                (Iterator<Long>[]) new Iterator[searchWords.size()];
        PageComparator toCompare = new PageComparator();
        PriorityQueue<Long> bestPageIndexes = new PriorityQueue<>(numResults, toCompare);
        long[] currentPageIndexes = new long[searchWords.size()];

        for (int i = 0; i < searchWords.size(); ++i) {
            String word = searchWords.get(i);
            Long index = word2index.get(word);
            InfoFile wordFile = wordDisk.get(index);
            List<Long> indices = wordFile.indices;
            Iterator<Long> iter = indices.iterator();
            wordFileIterators[i] = iter;
        }

        while (getNextPageIndexes(currentPageIndexes, wordFileIterators)) {
            if (allEqual(currentPageIndexes)) {
                String url = pageDisk.get(currentPageIndexes[0]).data;
                System.out.println(url);
                if (bestPageIndexes.size() != numResults) {
                    long index = url2index.get(url);
                    bestPageIndexes.offer(index);
                } else {
                    long peek = bestPageIndexes.peek();
                    if (peek < url2index.get(url)) {
                        bestPageIndexes.poll();
                        bestPageIndexes.offer(url2index.get(url));
                    }
                }
            }
        }

        String[] stringArray = new String[bestPageIndexes.size()];
        for(int i = stringArray.length - 1; i >= 0; --i) {
            long index = bestPageIndexes.poll();
         //  System.out.println(index + " has priority: " + pageDisk.get(index).priority);
            stringArray[i] = pageDisk.get(index).data;
        }

        return stringArray;
    }


    long indexPage(String url) {
        long index = pageDisk.newFile();
        InfoFile file = new InfoFile(url);
        pageDisk.put(index, file);
        url2index.put(url, index);
        System.out.println("indexing page " + index + " " + file);
        return index;
    }

    long indexWord(String word) {
        long index = wordDisk.newFile();
        InfoFile file = new InfoFile(word);
        wordDisk.put(index, file);
        word2index.put(word, index);
        System.out.println("indexing word " + index + " " + file);
        return index;
    }

    void rankSlow(double defaultPriority) {
        for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()) {
            long index = entry.getKey();
            InfoFile file = entry.getValue();
            if (file.indices.size() == 0) {
                continue;
            }
            double priorityPerIndex = file.priority / (double) file.indices.size();
            for (long page : file.indices) {
                InfoFile pageIndex = pageDisk.get(page);
                pageIndex.tempPriority += priorityPerIndex;
            }
        }
        for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()) {
            long index = entry.getKey();
            InfoFile file = entry.getValue();
            file.priority = file.tempPriority + defaultPriority;
            file.tempPriority = 0;
        }
    }

    void rankFast(double defaultPriority) {
        ArrayList<Vote> voteList = new ArrayList<>();
        for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()) {
            InfoFile file = entry.getValue();
            for (Long index : file.indices) {
                Vote vote = new Vote(index, file.priority / file.indices.size());
                voteList.add(vote);
            }
        }
        Collections.sort(voteList);
        Iterator<Vote> iterator = voteList.iterator();
        Vote currentVote = iterator.next();
        for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()) {
            long index = entry.getKey();
            InfoFile file = entry.getValue();
            while (currentVote.index == index) {
                file.tempPriority += currentVote.vote;
                if (iterator.hasNext())
                    currentVote = iterator.next();
                else
                    break;
            }
            file.priority = defaultPriority + file.tempPriority;
            file.tempPriority = 0;


        }
    }

    class PageComparator implements Comparator<Long> {

        public int compare(Long index1, Long index2) {
        InfoFile file1 = pageDisk.get(index1);
        double priorityIndex = file1.priority;
        InfoFile file2 = pageDisk.get(index2);
        double priorityIndex2 = file2.priority;
        if (priorityIndex < priorityIndex2) {
            return -1;
        } else if (priorityIndex > priorityIndex2) {
            return 1;
        } else {
            return 0;
        }
    }

}

    class Vote implements Comparable<Vote> {

        Vote(Long index, double vote) {
            this.index = index;
            this.vote = vote;
        }

        Long index;
        double vote;

        @Override
        public int compareTo(Vote o) {
            return (int) (index - o.index);
        }
    }

    private boolean allEqual(long[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (i > 0 && array[i] != array[i - 1]) {
                return false;
            }
        }

        return true;
    }

    private long getLargest(long[] array) {
        long largest = array[0];
        for (int i = 0; i < array.length; ++i) {
            if (array[i] > largest) {
                largest = array[i];
            }
        }
        return largest;
    }

    private boolean getNextPageIndexes
            (long[] currentPageIndexes, Iterator<Long>[] wordFileIterators) {

        if (allEqual(currentPageIndexes)) {
            for (int i = 0; i < currentPageIndexes.length; ++i) {
                if (!wordFileIterators[i].hasNext()) {
                    return false;
                } else {
                    currentPageIndexes[i] = wordFileIterators[i].next();
                }
            }

        } else {
            long largest = getLargest(currentPageIndexes);
            for (int i = 0; i < currentPageIndexes.length; ++i) {
                if (currentPageIndexes[i] != largest) {
                    if (!wordFileIterators[i].hasNext()) {
                        return false;
                    } else {
                        currentPageIndexes[i] = wordFileIterators[i].next();
                    }
                }

            }
        }
        return true;
    }
}


