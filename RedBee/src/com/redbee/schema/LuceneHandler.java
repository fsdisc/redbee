/*
 *  Red Bee Browser
 *
 *  Copyright (c) 2013 Tran Dinh Thoai <dthoai@yahoo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.redbee.schema;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneHandler extends Entity.Handler {
 
    protected String dirIndex = "";
    protected String dirBackup = "";

    public LuceneHandler(String folder) {
        this.dirIndex = new File(folder, "index").getAbsolutePath();
        this.dirBackup = new File(folder, "backup").getAbsolutePath();
        new File(dirIndex).mkdirs();
        new File(dirBackup).mkdirs();
    }
 
    public boolean exists(String id) {
        boolean tag = false;
        if (id.length() == 0) return tag;
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(dirIndex)));
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs td = searcher.search(new TermQuery(new Term(Entity.ID, id)), 1);
            if (td.totalHits > 0) {
                tag = true;
            }
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
  
        return tag; 
    }
 
    public void create(Entity src) {
        Monitor monitor = new Monitor();
        Timer timer = new Timer();
        timer.schedule(new CreateTask(timer, src, monitor), 1);
        while (!monitor.finished) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        timer = null;
    }

    protected void createEntity(Entity src) { 
        if (src.getId().length() == 0) return;
        if (src.getKind().length() == 0) return;

        try {
            backup(src);
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(FSDirectory.open(new File(dirIndex)), iwc);
            Document doc = new Document();
            write(src, doc);
            writer.addDocument(doc);
            writer.close();
        } catch (Exception e) {
        }
    }
 
    public void update(Entity src) {
        Monitor monitor = new Monitor();
        Timer timer = new Timer();
        timer.schedule(new UpdateTask(timer, src, monitor), 1);
        while (!monitor.finished) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        timer = null;
    }

    protected void updateEntity(Entity src) { 
        if (src.getId().length() == 0) return;
        if (src.getKind().length() == 0) return;

        try {
            backup(src);
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(FSDirectory.open(new File(dirIndex)), iwc);
            Document doc = new Document();
            write(src, doc);
            writer.updateDocument(new Term(Entity.ID, src.getId()), doc);
            writer.close();
        } catch (Exception e) {
        }
    }
 
    public void load(String id, Entity src) {
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(dirIndex)));
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs td = searcher.search(new TermQuery(new Term(Entity.ID, id)), 1);
            if (td.totalHits > 0) {
                Document doc = searcher.doc(td.scoreDocs[0].doc);
                if (allowLoad(id, doc.get(Entity.KIND))) {
                    src.setSchema(doc.get(Entity.SCHEMA));
                    read(src, doc);
                }
            }
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
    }
 
    protected boolean allowLoad(String id, String kind) {
        return true;
    }
 
    public int count(String kind, Query query, Filter filter, Sort sort, int max) {
        int tag = 0;
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(dirIndex)));
            IndexSearcher searcher = new IndexSearcher(reader);
            BooleanQuery boolQuery = new BooleanQuery();
            boolQuery.add(new BooleanClause(new TermQuery(new Term(Entity.KIND, kind)), Occur.MUST));
            if (query != null) {
                boolQuery.add(new BooleanClause(query, Occur.MUST));
            }
            TopDocs td = null;
            if (filter != null && sort != null) {
                td = searcher.search(boolQuery, filter, max, sort);
            } else if (filter != null) {
                td = searcher.search(boolQuery, filter, max);
            } else if (sort != null) {
                td = searcher.search(boolQuery, max, sort);
            } else {
                td = searcher.search(boolQuery, max);
            }
            tag = td.totalHits;
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
        return tag;
    }

    public int count(String kind, Query query, int max) {
        return count(kind, query, null, null, max);
    }

    public int count(String kind, Query query, Sort sort, int max) {
        return count(kind, query, null, sort, max);
    }
 
    public int count(String kind, Query query, Filter filter, int max) {
        return count(kind, query, filter, null, max);
    }
 
    public List<Entity> search(String kind, Query query, int max) {
        return search(kind, query, null, null, max);
    }

    public List<Entity> search(String kind, Query query, Sort sort, int max) {
        return search(kind, query, null, sort, max);
    }
 
    public List<Entity> search(String kind, Query query, Filter filter, int max) {
        return search(kind, query, filter, null, max);
    }
 
    public List<Entity> search(String kind, Query query, int pagesize, int pageno) { 
        return search(kind, query, null, null, pagesize, pageno);
    }
 
    public List<Entity> search(String kind, Query query, Sort sort, int pagesize, int pageno) { 
        return search(kind, query, null, sort, pagesize, pageno);
    }
 
    public List<Entity> search(String kind, Query query, Filter filter, int pagesize, int pageno) {
        return search(kind, query, filter, null, pagesize, pageno);
    }
 
    public List<Entity> search(String kind, Query query, Filter filter, Sort sort, int max) {
        List<Entity> tag = new ArrayList<Entity>();
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(dirIndex)));
            IndexSearcher searcher = new IndexSearcher(reader);
            BooleanQuery boolQuery = new BooleanQuery();
            boolQuery.add(new BooleanClause(new TermQuery(new Term(Entity.KIND, kind)), Occur.MUST));
            if (query != null) {
                boolQuery.add(new BooleanClause(query, Occur.MUST));
            }
            TopDocs td = null;
            if (filter != null && sort != null) {
                td = searcher.search(boolQuery, filter, max, sort);
            } else if (filter != null) {
                td = searcher.search(boolQuery, filter, max);
            } else if (sort != null) {
                td = searcher.search(boolQuery, max, sort);
            } else {
                td = searcher.search(boolQuery, max);
            }
            for (int i = 0; i < td.totalHits; i++) {
                Entity item = new Entity(this);
                Document doc = searcher.doc(td.scoreDocs[i].doc);
                item.setSchema(doc.get(Entity.SCHEMA));
                read(item, doc);
                tag.add(item);
            }
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
        return tag;
    }

    public List<Entity> search(String kind, Query query, Filter filter, Sort sort, int pagesize, int pageno) {
        List<Entity> tag = new ArrayList<Entity>();
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(dirIndex)));
            IndexSearcher searcher = new IndexSearcher(reader);
            BooleanQuery boolQuery = new BooleanQuery();
            boolQuery.add(new BooleanClause(new TermQuery(new Term(Entity.KIND, kind)), Occur.MUST));
            if (query != null) {
                boolQuery.add(new BooleanClause(query, Occur.MUST));
            }
            if (pagesize <= 0) pagesize = 10;
            if (pageno <= 0) pageno = 1;
            int max = pageno * pagesize;
            TopDocs td = null;
            if (filter != null && sort != null) {
                td = searcher.search(boolQuery, filter, max, sort);
            } else if (filter != null) {
                td = searcher.search(boolQuery, filter, max);
            } else if (sort != null) {
                td = searcher.search(boolQuery, max, sort);
            } else {
                td = searcher.search(boolQuery, max);
            }
            for (int i = (pageno - 1) * pagesize; i < td.totalHits && i < max; i++) {
                Entity item = new Entity(this);
                Document doc = searcher.doc(td.scoreDocs[i].doc);
                item.setSchema(doc.get(Entity.SCHEMA));
                read(item, doc);
                tag.add(item);
            }
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
        return tag;
    }
 
    protected void backup(Entity src) {
        String id = src.getId();
        if (id.length() == 0) return;
        String kind = src.getKind();
        if (kind.length() == 0) return;
        String fid = "";
        for (int i = 0; i < id.length() && i + 1 < id.length(); i += 2) {
            if (fid.length() > 0) fid += File.separator;
            fid += id.substring(i, i + 2);
        }
        try {
            File file = new File(dirBackup, kind);
            file = new File(file.getAbsolutePath(), fid);
            file.mkdirs();
            String folder = file.getAbsolutePath();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(folder, id + ".txt"))));
            writer.write(src.toString());
            writer.close();
        } catch (Exception e) {
        }
    }
 
    protected void read(Entity entity, Document doc) {
        String schema = doc.get(Entity.SCHEMA);
        if (schema == null) schema = "";
        String[] fields = schema.split("\\|");
        for (int i = 0; i < fields.length && i + 1 < fields.length; i+= 2) {
            String kind = fields[i];
            String fname = fields[i + 1];
            String val = doc.get(fname);
            if (val == null) val = "";
            if (Entity.ALL_KINDS.indexOf("|" + kind + "|") < 0) continue;
            entity.setString(fname, val);
        }
    }
 
    protected void write(Entity entity, Document doc) {
        String schema = entity.getSchema();
        if (schema == null) schema = "";
        String[] fields = schema.split("\\|");
        for (int i = 0; i < fields.length && i + 1 < fields.length; i+= 2) {
            String kind = fields[i];
            String fname = fields[i + 1];
            if (Entity.STRING.equalsIgnoreCase(kind)) {
                Field field = new Field(fname, entity.getString(fname), Store.YES, Index.NOT_ANALYZED_NO_NORMS);
                doc.add(field);
            } else if (Entity.DOUBLE.equalsIgnoreCase(kind)) {
                NumericField field = new NumericField(fname, Store.YES, true);
                field.setDoubleValue(entity.getDouble(fname));
                doc.add(field);
            } else if (Entity.FLOAT.equalsIgnoreCase(kind)) {
                NumericField field = new NumericField(fname, Store.YES, true);
                field.setFloatValue(entity.getFloat(fname));
                doc.add(field);
            } else if (Entity.INTEGER.equalsIgnoreCase(kind)) {
                NumericField field = new NumericField(fname, Store.YES, true);
                field.setIntValue(entity.getInteger(fname));
                doc.add(field);
            } else if (Entity.LONG.equalsIgnoreCase(kind)) {
                NumericField field = new NumericField(fname, Store.YES, true);
                field.setLongValue(entity.getLong(fname));
                doc.add(field);
            } else if (Entity.ANALYZED.equalsIgnoreCase(kind)) {
                Field field = new Field(fname, entity.getString(fname), Store.YES, Index.ANALYZED);
                doc.add(field);
            }
        }
    }
 
    public void delete(String id) {
        Monitor monitor = new Monitor();
        Timer timer = new Timer();
        timer.schedule(new DeleteTask(timer, id, monitor), 1);
        while (!monitor.finished) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        timer = null;
    }
 
    protected void deleteEntity(String id) { 
        if (id.length() == 0) return;
        String kind = "";
        
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(dirIndex)));
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs td = searcher.search(new TermQuery(new Term(Entity.ID, id)), 1);
            if (td.totalHits > 0) {
                Document doc = searcher.doc(td.scoreDocs[0].doc);
                kind = doc.get(Entity.KIND);
            }
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
        if (kind.length() == 0) return;
        if (!allowDelete(id, kind)) return;
        
        try {
            removeBackup(id, kind);
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(FSDirectory.open(new File(dirIndex)), iwc);
            writer.deleteDocuments(new Term(Entity.ID, id));
            writer.close();
        } catch (Exception e) {
        }
    }
 
    protected boolean allowDelete(String id, String kind) {
        return true;
    }
 
    protected void removeBackup(String id, String kind) {
        if (id.length() == 0) return;
        if (kind.length() == 0) return;
        String fid = "";
        for (int i = 0; i < id.length() && i + 1 < id.length(); i += 2) {
            if (fid.length() > 0) fid += File.separator;
            fid += id.substring(i, i + 2);
        }
        try {
            File file = new File(dirBackup, kind);
            file = new File(file.getAbsolutePath(), fid);
            String folder = file.getAbsolutePath();
            file = new File(folder, id + ".txt");
            file.delete();
        } catch (Exception e) {
        }
    }

    private class DeleteTask extends TimerTask {

        private String id;
        private Timer timer;
        private Monitor monitor;
  
        public DeleteTask(Timer timer, String id, Monitor monitor) {
            this.timer = timer;
            this.id = id;
            this.monitor = monitor;
        }
  
        @Override
        public void run() {
            deleteEntity(id);
            monitor.finished = true;
            timer.cancel();
            timer.purge();
            timer = null;
        }
  
    }

    private class CreateTask extends TimerTask {

        private Entity entity;
        private Timer timer;
        private Monitor monitor;
  
        public CreateTask(Timer timer, Entity entity, Monitor monitor) {
            this.timer = timer;
            this.entity = entity;
            this.monitor = monitor;
        }
  
        @Override
        public void run() {
            createEntity(entity);
            monitor.finished = true;
            timer.cancel();
            timer.purge();
            timer = null;
        }
  
    }

    private class UpdateTask extends TimerTask {

        private Entity entity;
        private Timer timer;
        private Monitor monitor;
  
        public UpdateTask(Timer timer, Entity entity, Monitor monitor) {
            this.timer = timer;
            this.entity = entity;
            this.monitor = monitor;
        }
  
        @Override
        public void run() {
            updateEntity(entity);
            monitor.finished = true;
            timer.cancel();
            timer.purge();
            timer = null;
        }
  
    }
 
    private class Monitor {
        public boolean finished = false;
    }
 
}