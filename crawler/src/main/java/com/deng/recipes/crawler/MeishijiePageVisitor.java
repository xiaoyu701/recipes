package com.deng.recipes.crawler;

import com.alibaba.fastjson.JSON;
import com.deng.recipes.entity.CookStep;
import com.deng.recipes.entity.Ingredient;
import com.deng.recipes.entity.Recipe;
import com.deng.recipes.entity.RecipeEntity;
import com.google.common.base.Preconditions;
import net.vidageek.crawler.Page;
import net.vidageek.crawler.Url;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

/**
 * Created by hcdeng on 2017/4/21.
 */
public class MeishijiePageVisitor extends AbstractPageVisitor<RecipeEntity> {


    public MeishijiePageVisitor(BlockingQueue<RecipeEntity> blockingQueue) {
        super(blockingQueue);
    }

    public boolean followUrl(Url url) {
        return url.toString().contains("/zuofa/");
    }

    protected RecipeEntity processPage(Page page) {

        if (!page.getUrl().contains("/zuofa/")) {
            return null;
        }

        try {
            FileWriter fileWriter = new FileWriter(
                    new File("D:\\data\\htmls" + page.getUrl().replaceAll("[^0-9a-zA-Z\\.]", "")));
            fileWriter.write(page.getContent());
            fileWriter.close();
        }catch (IOException e){}

        return processPageContent(page.getContent());


    }

    private static RecipeEntity processPageContent(String content){
        Preconditions.checkNotNull(content);
        Recipe recipe = new Recipe();
        Document doc = Jsoup.parse(content);

        Elements tmp = doc.select("h1"); // 标题
        recipe.setName(tmp.text());

        tmp = doc.select("div.cp_headerimg_w > img");
        String srcUrl = tmp.attr("src");
        recipe.setImages(srcUrl);

        tmp = doc.select("span[class=icon icon_nd] ~ a");
        recipe.setRecruit(tmp.text());

        tmp = doc.select("li[class=w127 bb0]  a");
        recipe.setTaste(tmp.text());

        tmp = doc.select("span[class=icon icon_zb] ~ a");
        recipe.setSetupTime(tmp.text());

        tmp = doc.select("span[class=icon icon_pr] ~ a");
        recipe.setCookingTime(tmp.text());

        tmp = doc.select("div.materials > p"); //功能
        recipe.setFuncational(tmp.text());

        //extract the main ingredient
        tmp = doc.select("div[class=yl zl clearfix] li");
        if(tmp != null){
            for(int i = 0; i < tmp.size(); i++){
                Ingredient ingredient = new Ingredient();
                Element e = tmp.get(i);
                ingredient.setIngredientName(e.select("h4 > a").text());
                ingredient.setUrl(e.select("img").attr("src"));
                ingredient.setQuantityDesc(e.select("span").text());
                recipe.addMainIngredient(ingredient);
            }
        }

        //extract the sub ingredient
        tmp = doc.select("div[class=yl fuliao clearfix] li");
        if(tmp != null){
            for(int i = 0; i < tmp.size(); i++){
                Ingredient ingredient = new Ingredient();
                Element e = tmp.get(i);
                ingredient.setIngredientName(e.select("a").text());
                ingredient.setQuantityDesc(e.select("span").text());
                recipe.addSubIngredient(ingredient);
            }
        }


        //extract steps
        List<CookStep> cookSteps = new ArrayList<>();
        tmp = doc.select("div.content.clearfix");
        if(tmp != null){
            for(int i = 0; i < tmp.size(); i++){
                CookStep cookStep = new CookStep();
                Element e = tmp.get(i);
                cookStep.setStepOrder(i);
                cookStep.setDescription(e.select("p").text());
                cookStep.setImage(e.select("img").attr("src"));
                cookSteps.add(cookStep);
            }
        }

        RecipeEntity recipeEntity = new RecipeEntity(recipe,cookSteps);

        return  recipeEntity;
    }

    public static void main(String[] args) throws IOException{
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(new File("D:\\data\\htmls\\htmlshttpwww.meishij.netzuofazhuganchaodoupi.html"));
        while(scanner.hasNextLine()){
            sb.append(scanner.nextLine()).append("\n\r");
        }
        scanner.close();

        System.out.println(JSON.toJSONString(processPageContent(sb.toString())));
    }
}
