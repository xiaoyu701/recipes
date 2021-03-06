package com.deng.recipes.api.extractor;

import com.deng.recipes.api.entity.CookStep;
import com.deng.recipes.api.entity.Ingredient;
import com.deng.recipes.api.entity.Recipe;
import com.deng.recipes.api.entity.RecipeEntity;
import com.google.common.base.Preconditions;
import org.elasticsearch.common.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hcdeng on 2017/4/26.
 */
public class DouguoRecipeExtractor extends RecipeExtractor {

    @Override
    public RecipeEntity extract(String content) {
        Preconditions.checkNotNull(content);
        Recipe recipe = new Recipe();
        Document doc = Jsoup.parse(content);

        if (!extractBasicInfos(recipe, doc)) return null;
        extractIngredients(recipe, doc);
        List<CookStep> cookSteps = extractCookSteps(doc);

        return new RecipeEntity(recipe, cookSteps);
    }

    private boolean extractBasicInfos(Recipe recipe, Document doc) {
        Element tmp = doc.getElementById("page_cm_id");
        if (tmp == null) return false;
        String title = tmp.text();

        recipe.setTitle(title);
        ////System.out.println(title);

        String desc = doc.select("div.xtip").text();
        recipe.setDesc(desc);
        ////System.out.println(desc);

        String img = doc.select("div.bmayi.mbm img").attr("src");
        recipe.setImages(Arrays.asList(img));

        String view = doc.select("span.collectview").text();
        String collection = doc.select("span.collectnum").text();
        recipe.setCollectedNum((Integer.parseInt(view) * 3 + Integer.parseInt(collection) * 7) / 10);

        String tips = doc.select("div.xtieshi p").text();
        ////System.out.println(tips);
        recipe.setTips(tips);

        return true;
    }

    private List<CookStep> extractCookSteps(Document doc) {
        //<div class="stepcont mll libdm pvl clearfix">
        Elements ses = doc.select("div.stepcont.mll.libdm.pvl.clearfix");
        List<CookStep> cookSteps = new ArrayList<>();
        for (int i = 0; i < ses.size(); i++) {
            Element e = ses.get(i);
            String sImg = e.select("a.cboxElement img").attr("original");
            String oStr = e.select("span.fwb").text();
            String sDesc = e.select("p").text().substring(oStr.length());
            ////System.out.println(sDesc + "->" + sImg);
            cookSteps.add(new CookStep(i + 1, sDesc, sImg));
        }
        return cookSteps;
    }

    private void extractIngredients(Recipe recipe, Document doc) {
        Elements table = doc.select("table.retamr");
        Elements tds = table.select("td");
        for (int i = 0; i < tds.size(); i++) {
            Element e = tds.get(i);
            Elements ke = e.select("span");
            String key = ke.text();
            String value = "";
            String url = "";
            if (Strings.isNullOrEmpty(key)) continue;

            if (key.contains("难度") ) {
                value = e.text();
                if(value.length() >key.length())
                    value = value.substring(key.length());
                recipe.setRecruit(value);
            } else if (key.contains("时间")) {
                value = e.text();
                if(value.length() >key.length())
                    value = value.substring(key.length());
                recipe.setCookingTime(value);
            } else {
                key = ke.get(0).text();
                url = ke.get(0).select("a").attr("href");
                value = ke.size() > 1 ? ke.get(1).text() : key;
                recipe.addIngredient(new Ingredient(key, value, url));
            }

            ////System.out.println(key + " --> " + value + " ->" + url);
        }
    }
}
